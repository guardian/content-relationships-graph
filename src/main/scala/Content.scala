import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.{ContentApiClient, GuardianContentClient}
import com.gu.contentatom.thrift.AtomType

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
    val search = ContentApiClient.search.q(term).pageSize(200)
    client.getResponse(search).map { response =>
      response.results
    }
  }

  def getLinksForArticle(id: String) = getArticle(id).map { maybeArticle =>
    maybeArticle.map { article =>
      ExtractThings.extractLinks(article)
    }
  }

  def getAtomUses(atom: Atom) = {
    val value = s"atom/${atom.atomType}/${atom.atomId}"
    val atomType = AtomType.valueOf(atom.atomType).getOrElse(AtomType.Media)

    val search = ContentApiClient.atomUsage(atomType, atom.atomId)

    client.getResponse(search).map { resp =>
      resp.results
    }

  }
}
