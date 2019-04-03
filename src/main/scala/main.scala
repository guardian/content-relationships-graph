import scala.concurrent.ExecutionContext.Implicits.global

object main {
  def main(args: Array[String]): Unit = {
    val things = Magic.getArticles
    things.map { articles =>
      articles
        .map { article =>
//          println(path)
          GraphStore
            .read(s"""
            |MERGE (a: Page {url:"${article.webUrl}"}) SET a.title="${article.webTitle}", a.path="${article.id}"
          """.stripMargin)

        }
      articles.map { article =>
        val links = Magic.getLinksForArticle(article.id)
        links.map { futureLink =>
          futureLink.map { link =>
            GraphStore.read(s"""
               |MERGE(a: Page {url:"${article.webUrl}"})
               |MERGE(b: Page {url:"${link.url}"})
               |MERGE (a)-[:Link {text: "${link.text}", source: "${link.source}"}]->(b)
             """.stripMargin)
          }
        }
      }
    }
  }
}
