import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.{ContentApiClient, GuardianContentClient}

import scala.concurrent.Future

object Magic {
  val key = Config.capi.key
  val client = new GuardianContentClient(key)
  // query for a single content item and print its web title
  implicit val ec = scala.concurrent.ExecutionContext.global

  def getLinksForArticle(path: String): Future[Seq[Link]] = {
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
        item =>
          item.content
            .map { content =>
              ExtractLinks.extractLinks(content)
            }
            .getOrElse(Seq()))
  }

  def getArticles: Future[Seq[Content]] = {
    val search = ContentApiClient.search.q("brexit")
    client.getResponse(search).map { response =>
      response.results
    }
  }
}
