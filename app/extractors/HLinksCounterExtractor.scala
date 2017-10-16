package extractors

import java.net.URL

import org.jsoup.nodes.Document
import play.api.libs.ws.WSClient

import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import collection.JavaConverters._

/**
  * Created by ERAN on 10/15/2017.
  */
object HLinksCounterExtractor extends DocExtractor {

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]] = {
    val documentDomain = (new URL(doc.baseUri())).getHost
    val linksDomains = doc.select("a[href]").asScala.flatMap(link => {
      Try(new URL(link.attr("abs:href"))).toOption
    })
    val (internalLinksCounter, externalLinksCounter) = linksDomains.foldLeft((0,0))((counters, link) => {
      if(link == documentDomain) (counters._1 + 1, counters._2) else (counters._1, counters._2 + 1)
    })
    Future.successful(Option((f"$internalLinksCounter Internal links $externalLinksCounter External links")))
  }

  override def getExtractorKey(): String = {
    "HyperMedia-Links-Counter"
  }
}