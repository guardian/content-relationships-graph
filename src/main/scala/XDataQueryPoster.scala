import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}
import spray.json._
import DefaultJsonProtocol.{jsonFormat3, _}

final case class XDataRequestParameters(path: String, capiKey: String)

trait XDataRequestProtocol extends DefaultJsonProtocol {
  implicit val requestFormat = jsonFormat2(XDataRequestParameters)
}

object XDataQueryPoster extends XDataRequestProtocol with SprayJsonSupport {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val http = Http(system)

  def makeXDataRequest(param: XDataRequestParameters): Future[HttpResponse] =
    Marshal(param).to[RequestEntity] flatMap { entity =>
      val request = HttpRequest(
        method = HttpMethods.POST,
        uri = "https://qtrogkrej5.execute-api.eu-west-1.amazonaws.com/PROD/getEntities",
        entity = entity)
      http.singleRequest(request)
    }

  def runXDataRequest(articlePath:String) = {
    val params = XDataRequestParameters(articlePath, Config.capi.key)
    makeXDataRequest(params) onComplete {
      case Failure(ex) => System.out.println(s"Failed to post $params, reason: $ex")
      case Success(response) => System.out.println(s"Server responded with $response")
    }
  }
}