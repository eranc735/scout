package extractors

import org.jsoup.nodes.Document
import play.api.libs.ws.WSClient
import scala.language.postfixOps

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}

object HeadingsLevelCountersExtractor extends DocExtractor {

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]] = {
    val NUM_OF_HEADINGS_LEVELS = 6
    val hTagsCounters = new ArrayBuffer[Int](NUM_OF_HEADINGS_LEVELS)
    val headings = for( i <- 1 to NUM_OF_HEADINGS_LEVELS) yield "h" + i
    val hTags = doc.select(headings.mkString(","))
    for( heading <- headings) {
      val numOfLevelTags = Option(hTags.select(heading)).map(_.size).getOrElse(0)
      hTagsCounters.append(numOfLevelTags)
    }
    Future.successful(Option(hTagsCounters.mkString(",")))
  }

  override def getExtractorKey(): String = {
    "Headings-Level-Count"
  }
}
