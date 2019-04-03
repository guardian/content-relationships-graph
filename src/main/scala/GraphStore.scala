 

import org.neo4j.driver.v1._

import scala.concurrent.Future
import scala.collection.JavaConverters._

implicit val ec = scala.concurrent.ExecutionContext.global

object Store {
  val driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "alex"))

  def write(something: String) = Future {
    val session = driver.session()
    val transaction = session.beginTransaction()
    transaction.run(something)
    transaction.success()
  }

  def read(something: String) = Future {
    val session = driver.session()
    val result = session.run(something) //can use a {} and parameters object?
    result.list().asScala
  }

  def close = driver.close()
}
