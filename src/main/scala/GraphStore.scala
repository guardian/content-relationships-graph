import org.neo4j.driver.v1._

import scala.concurrent.Future
import scala.collection.JavaConverters._

object GraphStore {
  implicit val ec = scala.concurrent.ExecutionContext.global

  val driver = GraphDatabase.driver(
    Config.database.uri,
    AuthTokens.basic(Config.database.username, Config.database.password))

  def write(something: String) = Future {
    val session = driver.session()
    val transaction = session.beginTransaction()
    transaction.run(something)
    transaction.success()
  }

  def read(something: String) = Future {
    val session = driver.session()
    val result = session.run(something) //can use a {} and parameters object?
    result.list().asScala.toList
  }

  def close = driver.close()
}
