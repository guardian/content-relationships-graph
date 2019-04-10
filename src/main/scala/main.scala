import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

object main {
  def main(args: Array[String]): Unit = {

    Future.sequence(
      Seq(
        Content
          .getArticles("amazon")
          .map { seq =>
            seq.foreach { content =>
              val path = content.id
              GraphStore.storePath(path)
            }
          },
        GraphStore.getPagesWithoutTitle
          .map { paths =>
            Future.sequence(paths.map { path =>
              GraphStore.storePath(path)
            })
          }
          .flatMap(identity),
        GraphStore.getAtoms().map { atoms =>
          Future.sequence(atoms.map { atom =>
            GraphStore.storeAtomUses(atom)
          })
        }
      ))
    //    andThen {
//      case Success(_) => System.exit(0)
//      case _          => System.exit(1)
//    }

  }

}
