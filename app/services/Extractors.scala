package services

import java.net.URL

import org.jsoup.nodes.{Document, DocumentType}
import play.api.Logger
import play.api.libs.ws._

import scala.concurrent.duration._
import scala.language.postfixOps
import collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext}
import scala.util.Try
import com.twitter.util.Future

/**
  * Created by ERAN on 10/14/2017.
  */
trait DocExtractor {

  def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]]

  def getExtractorKey(): String

}

object HTMLVersionExtractor extends DocExtractor {

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]] = {
    val docTypes = doc.childNodes.asScala.collect {
        case docType: DocumentType => {
          Logger.info(docType.toString)
        docType.attributes().asScala.map(attr => {
          Logger.info(attr.getKey + "-" + attr.getValue)
        })
        Option(docType.attr("publicid"))
      }
    }.flatten
    Future(Some(docTypes.mkString(",")))
  }

  override def getExtractorKey(): String = {
    "HTML-Version"
  }
}

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

object TitleExtractor extends DocExtractor {

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]] = {
    Future.successful(Some(doc.title()))
  }

  override def getExtractorKey(): String = {
   "Title"
  }
}

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


object ContainsLoginPageExtractor extends DocExtractor {

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext):Future[Option[String]] = {
    val isLoginExists = !doc.select("input[type=password]").isEmpty
    Future.successful(Option(isLoginExists.toString))
  }

  override def getExtractorKey(): String = {
    "Contains-Login-Page"
  }
}

object LinksValidationExtractor extends DocExtractor {

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext): Future[Option[String]] = {
    val links = doc.select("a[href]").asScala
    val result = Future.sequence(links.map(link => {
      link.liftT
        val linkURL = link.attr("abs:href")
        val result = (ws.url(link.attr("abs:href")).withRequestTimeout(30000 millis).get())
        result.map(response => linkURL -> response.status)
    }))

    val resultTxt = result.map(res => {
      val sb = new StringBuilder
      res.foreach(tuple => sb.append(tuple._1 + ":" + tuple._2 + "\n"))
      Some(sb.toString())
    })
    resultTxt
  }

  override def getExtractorKey(): String = {
    "Links-Validation"
  }
}

