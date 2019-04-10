import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

import scala.io.Source

import io.circe.parser
import io.circe.generic.semiauto.deriveDecoder

object main {
  def main(args: Array[String]): Unit = {

    /*
      Usage of the Sample file:
        val data: Option[XData] = XDataProcessing.readSampleFile()
        println(data)
     */

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
  }
}