
import com.gu.contentapi.client.{ContentApiClient, GuardianContentClient}

import scala.concurrent.ExecutionContext.Implicits.global

object main {
  def main(args: Array[String]): Unit = {
    val key = "asdf"
    val client = new GuardianContentClient(key)
    // query for a single content item and print its web title
    val a =ContentApiClient.item("/politics/2019/apr/02/mps-seek-to-stop-no-deal-brexit-by-tabling-article-50-bill").showAtoms("all").showBlocks("all").showElements("all").showPackages(true).showEditorsPicks(true).showReferences("all").showRelated(true).showSection(true).showTags("all")
  client.getResponse(a).foreach(item =>

println(item)
  )

  }
}
