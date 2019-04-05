import com.gu.contentapi.client.model.v1.ElementType.{RichLink, Text}
import com.gu.contentapi.client.model.v1.{BlockElement, Content}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._

case class Link(url: String, source: String, text: String)
case class Tweet(url: String, user: String)

object ExtractThings {
  def extractTweeter(url: String) = {
    url.split('/').lift(3).getOrElse("")
  }
  def extractTweets(content: Content): Seq[Tweet] = {
    (for {
      blocks <- content.blocks
      body <- blocks.body
    } yield
      for {
        block <- body
        element <- block.elements
      } yield
        for {
          tweet <- element.tweetTypeData
          url <- tweet.url
        } yield Tweet(url, extractTweeter(url))).getOrElse(Seq()).flatten
  }

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
          } yield
            List(Link(url, s"${source}RichLink", rl.linkText.getOrElse("")))
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
      .map(a => Link(a.attr("href"), s"${source}Link", a.text))
    val srcs = doc
      .body()
      .select("[src]")
      .asScala
      .toList
      .map(link => Link(link.attr("src"), s"${source}Src", link.text))
    hrefs ++ srcs
  }
}
