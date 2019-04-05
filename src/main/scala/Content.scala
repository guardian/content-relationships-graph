import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.{ContentApiClient, GuardianContentClient}

import scala.concurrent.Future

object Content {
  val key = Config.capi.key
  val client = new GuardianContentClient(key)
  // query for a single content item and print its web title
  implicit val ec = scala.concurrent.ExecutionContext.global

  def getArticle(path: String): Future[Option[Content]] = {
    val article = ContentApiClient
      .item(path)
      .showAtoms("all")
      .showBlocks("all")
      .showElements("all")
      .showPackages(true)
      .showEditorsPicks(true)
      .showReferences("all")
      .showRelated(true)
      .showSection(true)
      .showTags("all")

    client
      .getResponse(article)
      .map(
        item => item.content
      )
  }

  def getArticles(term: String): Future[Seq[Content]] = {
    val search = ContentApiClient.search.q(term)
    client.getResponse(search).map { response =>
      response.results
    }
  }
}
