import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

object main {
  def main(args: Array[String]): Unit = {
//    Content.getAtomUses(Atom("38ba0c81-6b48-453c-b025-b048ff48201c", "media"))
    Future.sequence(
      Seq(
//        Content
//          .getArticles("amazon")
//          .map { seq =>
//            seq.foreach { content =>
//              val path = content.id
//              GraphStore.storePath(path)
//            }
//          },
        GraphStore
          .read("""
                  |MATCH (atom:Atom) RETURN atom.type, atom.id
                """.stripMargin)
          .map { atom =>
            println(atom)
            atom.map { a =>
              val atom =
                Atom(a.get("atom.id").asString, a.get("atom.type").asString)
              Content.getAtomUses(atom).map { uses =>
                uses.map { use =>
                  GraphStore.storeAtom(atom,
                                       s"https://www.theguardian.com/$use")

                }
              }
            }
          })
//        GraphStore
//          .read("""
//        |MATCH (n:Page) WHERE NOT EXISTS (n.title) RETURN n.url
//      """.stripMargin)
//          .map { result =>
//            Future.sequence(
//              result
//                .map(record => record.get("n.url").asString)
//                .filter(_.contains("https://www.theguardian.com"))
//                .map { url =>
//                  val path = url.replace("https://www.theguardian.com", "")
//                  println(path)
//                  GraphStore.storePath(path)
//                })
//          .flatMap(identity)
    )
//    andThen {
//      case Success(_) => System.exit(0)
//      case _          => System.exit(1)
//    }

  }

}
