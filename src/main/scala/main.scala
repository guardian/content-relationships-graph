import scala.concurrent.ExecutionContext.Implicits.global

object main {
  def main(args: Array[String]): Unit = {
    println("hello")
    val atom = Atom("1b78011e-a4f7-4b7f-8cab-6967274ffd1b", "media")
    Content.getAtom(atom).map { a =>
      println(a)
    }
    //val things = Content.getArticles
//    things.map { articles =>
//      articles
//        .map { article =>
//          println(article.id)
//          Content.getArticle(article.id).map { content =>
//            content.map { c =>
//              val atoms = Atoms.extractAtoms(c)
//              println(atoms)
//              atoms.map { atom =>
//                println("helo")
//                val a = Content.getAtom(atom)
//                println(a)
//                a.map { aa =>
//                  println(aa)
//                }
//              }
//            }
//
//          }
//
//        }
//    }

  }
////    val things = Content.getArticles
////    things.map { articles =>
////      articles
////        .map { article =>
//////          println(path)
////          GraphStore
////            .read(s"""
////            |MERGE (a: Page {url:"${article.webUrl}"}) SET a.title="${article.webTitle}", a.path="${article.id}"
////          """.stripMargin)
////
////        }
////      articles.map { article =>
////        val links = Content.getLinksForArticle(article.id)
////        links.map { futureLink =>
////          futureLink.map { link =>
////            GraphStore.read(s"""
////               |MERGE(a: Page {url:"${article.webUrl}"})
////               |MERGE(b: Page {url:"${link.url}"})
////               |MERGE (a)-[:Link {text: "${link.text}", source: "${link.source}"}]->(b)
////             """.stripMargin)
////          }
////        }
////      }
//    GraphStore.read("""
//          |MATCH (n:Page) WHERE NOT EXISTS (n.title) RETURN n.url
//        """.stripMargin).map { result =>
//      result
//        .map(record => record.get("n.url").asString)
//        .filter(_.contains("https://www.theguardian.com"))
//        .map { url =>
//          val path = url.replace("https://www.theguardian.com", "")
//          Content.getArticle(path).map { maybeContent =>
//            maybeContent.map { content =>
//              GraphStore.storeArticle(content)
////              GraphStore.storeArticleLinks(content)
//            }
//          }
//
//        }
//
//    }
//  }

}
