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
    }.recover {
      case e: Exception => {
        println("NOOO")
        println(e)
      }
    }
  def read(something: String) =
    Future {
      val session = driver.session()
      val result = session.run(something) //can use a {} and parameters object?
      result.list().asScala.toList
    }.recover {
      case e: Exception => {
        println("NOOO")
        println(e)
      }
    }

  def close = driver.close()
}
//CREATE CONSTRAINT ON (n:Page) ASSERT n.uri IS UNIQUE;
