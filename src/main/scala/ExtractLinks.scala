import com.gu.contentapi.client.model.v1.Content

object ExtractLinks {
  def extractLinks(content: Content): Seq[String] = {
    val x = content.blocks.map(blocks =>
      blocks.main.toSeq ++ blocks.body.getOrElse(Seq())
    ).getOrElse(Seq()).flatMap(_.elements)
  }
}
