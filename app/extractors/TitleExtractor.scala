package extractors

import org.jsoup.nodes.Document
import play.api.libs.ws.WSClient

import scala.language.postfixOps


import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by ERAN on 10/15/2017.
  */

object TitleExtractor extends DocExtractor {

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]] = {
    Future.successful(Option(doc.title()))
  }

  override def getExtractorKey(): String = {
    "Title"
  }
}