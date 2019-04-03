import com.gu.contentapi.client.model.v1.Content
import org.neo4j.driver.v1._

import scala.concurrent.Future
import scala.collection.JavaConverters._

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
    }
  def read(something: String): Future[List[Record]] = {
    println("HELLO")
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
                |MERGE (a: Page {url:"${content.webUrl}"}) SET a.title="${content.webTitle}", a.path="${content.id}
                "
              """.stripMargin)
  }
  def storeArticleLinks(content: Content) = {
    val path = content.id
    val links = ExtractLinks.extractLinks(content)
    links.map { link =>
      GraphStore.read(s"""
                   |MERGE(a: Page {url:"${content.webUrl}"})
                   |MERGE(b: Page {url:"${link.url}"})
                   |MERGE (a)-[:Link {text: "${link.text}", source: "${link.source}"}]->(b)
                 """.stripMargin)

    }
  }
  def close = driver.close()
}
//CREATE CONSTRAINT ON (n:Page) ASSERT n.uri IS UNIQUE;
