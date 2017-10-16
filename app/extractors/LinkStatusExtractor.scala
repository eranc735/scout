package extractors

import org.jsoup.nodes.Document
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.concurrent.duration._
import scala.language.postfixOps

import collection.JavaConverters._

/**
  * Created by ERAN on 10/15/2017.
  */

object LinkStatusExtractor extends DocExtractor {

  val maxTimeout = Duration(15, SECONDS)

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]] = {
    val links = doc.select("a[href]").asScala
    val result = Future.sequence(links.flatMap(link => {
      val linkURL = link.attr("abs:href")
      val request = Try(ws.url(link.attr("abs:href")).withMethod("HEAD").withFollowRedirects(true).withRequestTimeout(maxTimeout).get).toOption
      request.map(_.map(res => {
        linkURL -> res.status.toString
      }).recover { case t => linkURL ->  t.getMessage})
    }))

    val resultTxt = result.map(res => {
      val sb = new StringBuilder
      res.foreach(linkResponse => {
        sb.append(formatLinkStatusLine(linkResponse._1, linkResponse._2))
      })
      Some(sb.mkString)
    })
    resultTxt
  }

  def formatLinkStatusLine(link: String, status: String): String = {
    link + ":&nbsp&nbsp&nbsp&nbsp&nbsp" + status +"<br/>"
  }

  override def getExtractorKey(): String = {
    "Links-Validation"
  }
}

