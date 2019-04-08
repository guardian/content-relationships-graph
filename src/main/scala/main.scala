import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

object main {
  def main(args: Array[String]): Unit = {
    Future.sequence(
      Seq(
        Content
          .getArticles("amazon")
          .map { seq =>
            seq.foreach { content =>
              val path = content.id
              GraphStore.storePath(path)
            }
          },
        GraphStore
          .read("""
        |MATCH (n:Page) WHERE NOT EXISTS (n.title) RETURN n.url
      """.stripMargin)
          .map { result =>
            Future.sequence(
              result
                .map(record => record.get("n.url").asString)
                .filter(_.contains("https://www.theguardian.com"))
                .map { url =>
                  val path = url.replace("https://www.theguardian.com", "")
                  println(path)
                  GraphStore.storePath(path)
                })

          }
          .flatMap(identity)
      ))
//    andThen {
//      case Success(_) => System.exit(0)
//      case _          => System.exit(1)
//    }

  }

}
