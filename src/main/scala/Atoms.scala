import com.gu.contentapi.client.model.v1.Content

import scala.collection.immutable

case class Atom(atomId: String, atomType: String)

object Atoms {

  def extractAtoms(content: Content): Seq[Atom] = {
    for {
      blocks <- content.blocks.toList
      allBlocks <- (blocks.body.toList.flatten ++ blocks.main.toList)
      element <- allBlocks.elements.toList
      atom <- element.contentAtomTypeData.toList
    } yield Atom(atom.atomId, atom.atomType)

  }

}
