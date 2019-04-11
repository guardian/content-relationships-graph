import com.gu.contentapi.client.model.v1.Content
import org.neo4j.driver.v1._

import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.util.Success

object GraphStore {
  implicit val ec = scala.concurrent.ExecutionContext.global

  val driver = GraphDatabase.driver(
    Config.database.uri,
    AuthTokens.basic(Config.database.username, Config.database.password))

  def read(something: String): Future[List[Record]] = synchronized {
    val f = Future {

      val session = driver.session()
      val result = session.run(something) //can use a {} and parameters object?
      result.list().asScala.toList
    }
    f.onFailure { case t => println(s"Exception: ${t.getMessage}") }
    f
  }
  def read(something: String,
           parameters: Map[String, AnyRef]): Future[List[Record]] = {
    val f = Future {
      val session = driver.session()
      val result = session.run(something, parameters.asJava) //can use a {} and parameters object?

      result.list().asScala.toList
    }
    f.onFailure { case t => println(s"Exception: ${t.getMessage}") }
    f
  }
  def storeArticle(page: Page) = {
    read(
      "MERGE (a: Page {url: {url}}) SET a.title={title}, a.path={path}, a.image= {image}, a.published=DateTime({published}) ",
      Map(
        "url" -> page.url,
        "path" -> page.path,
        "image" -> page.image,
        "published" -> page.published,
        "title" -> page.title
      )
    )
  }

  def storeArticleLinks(content: Content) = {
    val path = content.id
    val links = ExtractThings.extractLinks(content)
    links.map { link =>
      read(s"""
                   |MERGE(a: Page {url:"${content.webUrl}"})
                   |MERGE(b: Page {url:"${link.url}"})
                   |MERGE (a)-[:Link {text: "${link.text}", source: "${link.source}"}]->(b)
                 """.stripMargin)
    }
  }

  def storeArticleTweets(content: Content) = {
    val tweets = ExtractThings.extractTweets(content)
    tweets.map { tweet =>
      read(s"""
           |MERGE(tweet: Tweet {url: "${tweet.url}", user: "${tweet.user}"})
           |MERGE(page: Page {url:"${content.webUrl}"})
           |MERGE (page)-[:Contatins]->(tweet)
         """.stripMargin)
    }
  }

  def getAtoms() = {
    GraphStore
      .read("""
              |MATCH (atom:Atom) RETURN atom.type, atom.id
            """.stripMargin)
  }.map { atom =>
    atom.map { a =>
      Atom(a.get("atom.id").asString, a.get("atom.type").asString)
    }
  }

  def getPagesWithoutTitle: Future[List[String]] = {
    GraphStore
      .read("""
              |MATCH (n:Page) WHERE NOT EXISTS (n.title) RETURN n.url
            """.stripMargin)
      .map { records =>
        records
          .map(record => record.get("n.url").asString)
          .filter(_.contains("https://www.theguardian.com"))
          .map(_.replace("https://www.theguardian.com", ""))
      }
  }

  def storeAtomUses(atom: Atom) = {
    Content
      .getAtomUses(atom)
      .map { uses =>
        Future.sequence(
          uses.map { use =>
            GraphStore.storeAtom(atom, s"https://www.theguardian.com/$use")
          }
        )
      }
      .flatMap(identity)
  }

  def storeAtoms(content: Content) = {
    Atoms.extractAtoms(content).map { atom =>
      storeAtom(atom, content.webUrl)
    }
  }

  def storeAtom(atom: Atom, url: String) = {
    read(s"""
         |MERGE(atom: Atom {type: "${atom.atomType}", id: "${atom.atomId}"})
         |MERGE(page: Page {url: "$url"})
         |MERGE (page)-[:Contains]->(atom) 
       """.stripMargin)
  }

  def storeTags(content: Content) = {
    Tags.extractTags(content: Content).map { tag =>
      storeTag(tag, content.webUrl)
    }
  }

  def storeTag(tag: Tag, url: String) = {
    read(
      """
            |MERGE(tag: Tag {id: {id}, id: {description}})
            |MERGE(page: Page {url: {url}})
            |MERGE (page)-[:Related]->(tag)
       """.stripMargin,
      Map(
        "id" -> tag.id,
        "description" -> tag.description,
        "url" -> url
      )
    )
  }

  def storePath(path: String) = {
    Content
      .getArticle(path)
      .map { maybeContent =>
        val content = maybeContent.get // This future should fail if we don't have content
        val page = Page(content.webUrl,
                        Content.cleanTitle(content.webTitle),
                        content.id,
                        Content.getThumb(content).getOrElse(""),
                        content.webPublicationDate.map(_.iso8601).get)
        Future.sequence(
          Seq(GraphStore.storeArticle(page)) ++
            GraphStore.storeArticleLinks(content) ++
            // GraphStore.storeArticleTweets(content) ++
            GraphStore.storeAtoms(content) ++
            GraphStore.storeTags(content))
      }
      .flatMap(identity)
  }
  def fetchArticle(path: String) = {
    println(path)
    read(
      "MATCH (a: Page {path: {path}}) RETURN a.url, a.title, a.path, a.image, a.published",
      Map("path" -> path)) map { result =>
      result.headOption.map(_.asMap.asScala).map { row =>
        (row.get("a.url"),
         row.get("a.title"),
         row.get("a.path"),
         row.get("a.image"),
         row.get("a.published").map(_.toString))
      } match {
        case Some(
            (Some(url: String),
             Some(title: String),
             Some(path: String),
             Some(image: String),
             Some(published: String))) =>
          Some(Page(url, title, path, image, published))
        case _ => None

      }
    }
  }
  def fetchOutboundLinks(path: String) = {
    read(
      "MATCH (:Page {path:{path}})-[l:Link]->(a:Page) return l.text, a.url, a.title, a.path, a.image, a.published",
      Map("path" -> path)) map { result =>
      result.map(_.asMap.asScala) flatMap { row =>
        if (!row.values.exists(_ == null)) {
          (row.get("l.text"),
           row.get("a.url"),
           row.get("a.title"),
           row.get("a.path"),
           row.get("a.image"),
           row.get("a.published").map(_.toString)) match {
            case (Some(link: String),
                  Some(url: String),
                  Some(title: String),
                  Some(path: String),
                  Some(image: String),
                  Some(published: String)) =>
              Some(Right(link -> Page(url, title, path, image, published)))
            case _ => None
          }
        } else {
          (row.get("l.text"), row.get("a.url")) match {
            case (Some(link: String), Some(url: String)) =>
              Some(
                Left(link -> url.replace("https://www.theguardian.com/", "")))
            case _ => None
          }
        }
      }
    }
  }

  def fetchParentLinks(path: String, in: Boolean) = {
    val arrow = if (in) ("<-", "-") else ("-", "->")
    read(
      s"""MATCH (:Page {path:{path}})${arrow._1}[:Link]${arrow._2}(:Page)${arrow._1}[:Link]${arrow._2}(a:Page) return a.url, a.title, a.path, a.image, a.published""",
      Map("path" -> path)
    ) map { result =>
      println(result)
      result.map(_.asMap.asScala) flatMap { row =>
        println(row)
        if (!row.values.exists(_ == null)) {
          (row.get("a.url"),
           row.get("a.title"),
           row.get("a.path"),
           row.get("a.image"),
           row.get("a.published").map(_.toString)) match {
            case (Some(url: String),
                  Some(title: String),
                  Some(path: String),
                  Some(image: String),
                  Some(published: String)) =>
              Some(Right(Page(url, title, path, image, published)))
            case _ => None
          }
        } else {
          row.get("a.url") match {
            case Some(url: String) =>
              Some(Left(url.replace("https://www.theguardian.com/", "")))
            case _ => None
          }
        }
      }
    }
  }

  def fetchInboundLinks(path: String) = {
    read(
      "MATCH (:Page {path:{path}})<-[l:Link]-(a:Page) return l.text, a.url, a.title, a.path, a.image, a.published",
      Map("path" -> path)) map { result =>
      result.map(_.asMap.asScala) flatMap { row =>
        if (!row.values.exists(_ == null)) {
          (row.get("l.text"),
           row.get("a.url"),
           row.get("a.title"),
           row.get("a.path"),
           row.get("a.image"),
           row.get("a.published").map(_.toString)) match {
            case (Some(link: String),
                  Some(url: String),
                  Some(title: String),
                  Some(path: String),
                  Some(image: String),
                  Some(published: String)) =>
              Some(Right(link -> Page(url, title, path, image, published)))
            case _ => None
          }
        } else {
          (row.get("l.text"), row.get("a.url")) match {
            case (Some(link: String), Some(url: String)) =>
              Some(
                Left(link -> url.replace("https://www.theguardian.com/", "")))
            case _ => None
          }
        }
      }
    }
  }

  def getAtoms(path: String) = {
    read(
      "MATCH (: Page {path: {path}})-[:Contains]->(a: Atom) RETURN a.type, a.id",
      Map("path" -> path)) map { atoms =>
      atoms map (_.asMap.asScala) flatMap { atom =>
        (atom.get("a.type"), atom.get("a.id")) match {
          case (Some(t: String), Some(i: String)) => Some(Atom(i, t))
          case _                                  => None
        }
      }
    }
  }
  def getAtomUses(atom: Atom) = {
    println(atom)
    read(
      "MATCH (p: Page)-[:Contains]->(a: Atom {id: {id}, type: {type}}) RETURN p.url, p.title, p.path, p.image, p.published",
      Map("id" -> atom.atomId, "type" -> atom.atomType)
    ) map { result =>
      result.map(_.asMap.asScala) flatMap { row =>
        println(row)
        (row.get("p.url"),
         row.get("p.title"),
         row.get("p.path"),
         row.get("p.image"),
         row.get("p.published").map(_.toString)) match {
          case (Some(url: String),
                Some(title: String),
                Some(path: String),
                Some(image: String),
                Some(published: String)) =>
            Some(Page(url, title, path, image, published))
          case _ => None
        }
      }
    }
  }
  def close = driver.close()
}
//CREATE CONSTRAINT ON (n:Page) ASSERT n.uri IS UNIQUE;
