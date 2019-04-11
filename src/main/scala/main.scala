import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.io.Source
import io.circe.parser
import io.circe.generic.semiauto.deriveDecoder
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import scala.io.StdIn

object main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
                              "<h1>Say hello to akka-http</h1>"))
        }
      } ~ path("load") {
        post {
          onComplete(load) {
            case Success(_) =>
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
                                  s"<h1>yeah okay</h1>"))
            case Failure(ex) =>
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
                                  s"<h1>oh no ${ex}</h1>"))
          }
        }
      } ~ pathPrefix("content" / Remaining) { contentPath =>
        pathEnd {
          get {
            onComplete(
              GraphStore.fetchArticle(contentPath)
            ) {
              case Success(Some(article)) =>
                complete(article.asJson.toString)
              case Success(None) =>
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
                                    s"<h1>oh no not found</h1>"))
              case Failure(ex) =>
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
                                    s"<h1>oh no ${ex}</h1>"))
            }

          }
        }
      } ~ pathPrefix("linksout" / Remaining) { contentPath =>
        pathEnd {
          get {
            onComplete(
              GraphStore.fetchOutboundLinks(contentPath)
            ) {
              case Success(links) =>
                complete(links.asJson.toString)
              case Failure(ex) =>
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
                                    s"<h1>oh no ${ex}</h1>"))
            }
          }
        }
      } ~ pathPrefix("linksin" / Remaining) { contentPath =>
        pathEnd {
          get {
            onComplete(
              GraphStore.fetchInboundLinks(contentPath)
            ) {
              case Success(links) =>
                complete(links.asJson.toString)
              case Failure(ex) =>
                complete(
                  HttpEntity(ContentTypes.`text/html(UTF-8)`,
                             s"<h1>oh no ${ex}</h1>"))
            }
          }
        }
      }
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def load() = {

    /*
      Usage of the Sample file:
        val data: Option[XData] = XDataProcessing.readSampleFile()
        println(data)
     */

    Future.sequence(
      Seq(
        GraphStore.read(
          "CREATE CONSTRAINT ON (n:Page) ASSERT n.uri IS UNIQUE;"),
//        GraphStore.read(
//          "CREATE CONSTRAINT ON (n:Page) ASSERT n.path IS UNIQUE;"),
        Content
          .getArticles("assange")
          .map { seq =>
            seq.foreach { content =>
              val path = content.id
              GraphStore.storePath(path)
            }
          },
        GraphStore.getPagesWithoutTitle
          .map { paths =>
            Future.sequence(paths.map { path =>
              GraphStore.storePath(path)
            })
          }
          .flatMap(identity),
        GraphStore.getAtoms().map { atoms =>
          Future.sequence(atoms.map { atom =>
            GraphStore.storeAtomUses(atom)
          })
        }
      ))
  }
}
