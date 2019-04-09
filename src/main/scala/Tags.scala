import com.gu.contentapi.client.model.v1.Content

import scala.collection.immutable

case class Tag(id: String, description: String)

object Tags {
  def extractTags(content: Content): Seq[Tag] = {
    content.tags.map(tag => Tag(tag.id, tag.description.getOrElse("missing description")))
  }
}
