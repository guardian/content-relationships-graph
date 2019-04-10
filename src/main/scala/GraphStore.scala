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

  def write(something: String) =
    Future {
      val session = driver.session()
      val transaction = session.beginTransaction()
      transaction.run(something)
      transaction.success()
      println("executed!")
      transaction.close()
    }
  def read(something: String): Future[List[Record]] = {
    val f = Future {
      val session = driver.session()
      val result = session.run(something) //can use a {} and parameters object?
      result.list().asScala.toList
    }
    f.onFailure { case t => println(s"Exception: ${t.getMessage}") }
    f
  }

  def storeArticle(content: Content) = {
    read(s"""
                |MERGE (a: Page {url:"${content.webUrl}"}) SET a.title="${content.webTitle
              .replace('"', '`')}", a.path="${content.id}", a.image="${Content
              .getThumb(content)
              .get}", a.published="${content.webPublicationDate
              .map(_.iso8601)
              .get}"
              """.stripMargin)
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
  def storePath(path: String) = {
    Content
      .getArticle(path)
      .map { maybeContent =>
        val content = maybeContent.get //This future should fail if we don't have content
        Future.sequence(
          Seq(GraphStore.storeArticle(content)) ++ GraphStore
            .storeArticleLinks(content) ++
//            GraphStore.storeArticleTweets(content) ++
            GraphStore.storeAtoms(content))
      }
      .flatMap(identity)
  }

  def close = driver.close()
}
//CREATE CONSTRAINT ON (n:Page) ASSERT n.uri IS UNIQUE;
