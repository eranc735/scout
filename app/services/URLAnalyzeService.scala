package services

import models.{AnalysisElement, URLAnalysis}
import org.apache.commons.validator.routines.UrlValidator
import play.Logger
import play.api.libs.ws.WSClient
import org.jsoup.Jsoup
import org.jsoup.Connection.Method.GET
import URLAnalyzeService._
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import extractors.DocExtractor
import org.jsoup.nodes.Document

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.language.postfixOps


/**
  * Created by ERAN on 10/14/2017.
  */
trait URLAnalyzeService {

  implicit val ec: ExecutionContext

  import scala.language.implicitConversions

  val maxTimeOut =  Duration(20, SECONDS).toMillis.toInt

  lazy val cache: Cache[String, Document] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(1 hour)
      .maximumSize(1000)
      .build[String, Document]()

  def analyze(url: String, extractors: Seq[DocExtractor])(implicit ws: WSClient): Future[Try[URLAnalysis]] = {
    val urlValidator: UrlValidator = new UrlValidator()
    if(!urlValidator.isValid(url)) {
      return  Future.successful(Success(URLAnalysis(false, STATUS_UNDEFINED, Seq.empty)))
    }
    val document = getCachedDocument(url)
    document match {
      case doc: Success[Document] => extractFromDoc(doc.get, extractors)
      case Failure(t) => {
        Logger.error("error occurred while trying to analyze url %s error details: %s".format(url, t.getMessage))
        Future.successful(Failure[URLAnalysis](t))
      }
    }
  }

  private def getCachedDocument(url: String): Try[Document] = {
    val cachedDoc = cache.getIfPresent(url)
    cachedDoc match {
      case Some(doc) => {
        Success(doc)
      }
      case None => {
        val document = Try(Jsoup.connect(url).method(GET).timeout(maxTimeOut).get)
        document match {
          case Success(doc) => {
            cache.put(url, doc)
            Success(doc)
          }
          case f => f
        }
      }
    }
  }

  private def extractFromDoc(doc: Document, extractors: Seq[DocExtractor])(implicit ws: WSClient) = {
    val extractedData = Future.sequence(extractors.map(extractor => {
      extractor.extract(doc).map(value => AnalysisElement(extractor.getExtractorKey(), value.getOrElse(UNDEFINED)))
    }))
    extractedData.map(data => Success(URLAnalysis(true, 200, data)))
  }

}

object URLAnalyzeService {
  val STATUS_UNDEFINED = -1
  val UNDEFINED = "Undefined"
}

