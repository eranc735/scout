package extractors

import org.jsoup.nodes.{Document, DocumentType}
import play.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import collection.JavaConverters._

/**
  * Created by ERAN on 10/15/2017.
  */

object HTMLVersionExtractor extends DocExtractor {


  val htmlVersionPattern = """W3C//DTD (\w+\s[\d.]+)""".r


  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]] = {
    val versions = doc.childNodes.asScala.collect {
      case docType: DocumentType => {
        val version = if (docType.hasAttr("name") && docType.attr("name") == "html") {
          if (docType.hasAttr("publicid") && !docType.attr("publicid").isEmpty) {
            val matched = htmlVersionPattern.findFirstMatchIn(docType.attr("publicid"))
            matched.map(m => m.group(1))
          } else {
            Some("HTML5")
          }
        } else {
          None
        }
        version
      }
    }.flatten
    Future.successful(versions.lift(0))
  }

  override def getExtractorKey(): String = {
    "HTML-Version"
  }
}