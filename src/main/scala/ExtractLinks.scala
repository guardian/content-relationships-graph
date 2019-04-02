import com.gu.contentapi.client.model.v1.ElementType.RichLink
import com.gu.contentapi.client.model.v1.{BlockElement, Content}

object ExtractLinks {
  def extractLinks(content: Content): Seq[String] = {
    val elements = content.blocks.map(blocks =>
      blocks.main.toSeq ++ blocks.body.getOrElse(Seq())
    ).getOrElse(Seq()).flatMap(_.elements)
    elements.map {element =>
      element.`type` match {
        case RichLink => element.richLinkTypeData.get.originalUrl.getOrElse("").toString()
        case _ => ""
      }
    }.filter( _.size != 0  )
  }
}
