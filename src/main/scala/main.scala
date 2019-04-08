import scala.concurrent.ExecutionContext.Implicits.global

object main {
  def main(args: Array[String]): Unit = {
    Content
      .getArticles("brexit")
      .foreach { seq =>
        seq.foreach { content =>
          Content
            .getArticle(content.id)
            .foreach { maybeContent =>
              maybeContent.foreach { content =>
                GraphStore.storeArticle(content)
                GraphStore.storeArticleLinks(content)
                GraphStore.storeArticleTweets(content)
                GraphStore.storeAtoms(content)
              }
            }
        }
      }
  }
}
