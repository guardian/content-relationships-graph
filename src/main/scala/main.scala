import scala.concurrent.ExecutionContext.Implicits.global

object main {
  def main(args: Array[String]): Unit = {

    // Extract CAPI object for one Article and print to the terminal
    /*
    Content.getArticle("/politics/2019/apr/03/mps-pass-motion-to-debate-article-50-extension-by-one-vote")
        .foreach{maybeContent =>
          maybeContent.foreach{content =>
            println(content)
          }
        }
    */

    // Extract CAPI objects for a CAPI search term, and print to the screen
    /*
    Content.getArticles("brexit")
      .foreach{seq =>
        seq.foreach{content =>
          println(content)
        }
      }
    */

    // Extract Links from Content (CAPI object)
    /*
    Content.getArticle("/politics/2019/apr/03/mps-pass-motion-to-debate-article-50-extension-by-one-vote")
        .foreach{ optContent =>
          optContent.foreach{ content =>
            ExtractLinks.extractLinks(content).foreach{ link =>
              println(link)
            }
          }
        }
    */


    Content.getArticles("brexit").foreach{ articles =>
      articles.foreach{ content =>
        ExtractLinks.extractLinks(content).foreach{ link =>
          println(link)
        }
      }
    }

    /*
    val things = Content.getArticles
    things.map { articles =>
      articles
        .map { article =>
          GraphStore
            .read(s"""
            |MERGE (a: Page {url:"${article.webUrl}"}) SET a.title="${article.webTitle}", a.path="${article.id}"
          """.stripMargin)
        }
      articles.map { article =>
        val links = Content.getLinksForArticle(article.id)
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
    */

    /*
    GraphStore.read("""
          |MATCH (n:Page) WHERE NOT EXISTS (n.title) RETURN n.url
        """.stripMargin).map { result =>
      result
        .map(record => record.get("n.url").asString)
        .filter(_.contains("https://www.theguardian.com"))
        .map { url =>
          val path = url.replace("https://www.theguardian.com", "")
          Content.getArticle(path).map { maybeContent =>
            maybeContent.map { content =>
              GraphStore.storeArticle(content)
              // GraphStore.storeArticleLinks(content)
            }
          }
        }
    }
    */

  }
}
