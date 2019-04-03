import com.gu.contentapi.client.model.v1.ElementType.{RichLink, Text}
import com.gu.contentapi.client.model.v1.{BlockElement, Content}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._

case class Link(url: String, source: String)

object ExtractLinks {
  def extractLinks(content: Content): Seq[Link] = {
    val bodyLinks = for {
      blocks <- content.blocks
      body <- blocks.body
    } yield {
      extractLinksFromElements(body.flatMap(_.elements), "body")
    }

    val mainLinks = for {
      blocks <- content.blocks
      main <- blocks.main
    } yield {
      extractLinksFromElements(main.elements, "body")
    }
    bodyLinks.getOrElse(List()) ++ mainLinks.getOrElse(List())
  }

  def extractLinksFromElements(elements: Iterable[BlockElement],
                               source: String): List[Link] = {
    elements.toList.flatMap { element =>
      (element.`type` match {
        case RichLink =>
          for {
            rl <- element.richLinkTypeData
            url <- rl.originalUrl
          } yield List(Link(url, s"${source}RichLink"))
        case Text =>
          for {
            ttd <- element.textTypeData
            html <- ttd.html
          } yield extractLinksFromText(html, source)
        case _ => None
      }).getOrElse(List())
    }
  }

  def extractLinksFromText(string: String, source: String): List[Link] = {
    val doc = Jsoup.parse(string)
    val hrefs = doc
      .body()
      .select("[href]")
      .asScala
      .toList
      .map(_.attr("href"))
      .map(Link(_, s"${source}Link"))
    val srcs = doc
      .body()
      .select("[src]")
      .asScala
      .toList
      .map(_.attr("src"))
      .map(Link(_, s"${source}Src"))
    hrefs ++ srcs
  }
}
