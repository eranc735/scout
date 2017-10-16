package extractors

import org.jsoup.nodes.Document
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by ERAN on 10/15/2017.
  */

trait DocExtractor {

  def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]]

  def getExtractorKey(): String

}

case class DocElement(key: String, value: String)

