import com.gu.contentapi.client.model.v1.{BlockElement, Content}

object ExtractLinks {
  def extractLinks(content: Content): Seq[BlockElement] = {
    content.blocks.map(blocks =>
      blocks.main.toSeq ++ blocks.body.getOrElse(Seq())
    ).getOrElse(Seq()).flatMap(_.elements)
  }
}
