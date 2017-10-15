package services

import models.{AnalysisElement, URLAnalysis}
import org.apache.commons.validator.routines.UrlValidator
import play.Logger
import play.api.libs.ws.WSClient
import org.jsoup.Jsoup
import org.jsoup.Connection.Method.GET
import URLAnalyzeService._
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


/**
  * Created by ERAN on 10/14/2017.
  */
trait URLAnalyzeService {

  implicit val ec: ExecutionContext

  import scala.language.implicitConversions

  val maxTimeOut = 20*1000

  def analyze(url: String, extractors: Seq[DocExtractor])(implicit ws: WSClient): Future[Try[URLAnalysis]] = {
    val urlValidator: UrlValidator = new UrlValidator()
    if(!urlValidator.isValid(url)) {
      return  Future.successful(Success(URLAnalysis(false, STATUS_UNDEFINED, Seq.empty)))
    }
    val document = Try(Jsoup.connect(url).method(GET).timeout(maxTimeOut).get)
    document match {
      case doc: Success[Document] => {
        val extractedData = Future.sequence(extractors.map(extractor => {
          extractor.extract(doc.get).map(value => AnalysisElement(extractor.getExtractorKey(), value.getOrElse(UNDEFINED)))
        }))
        extractedData.map(data => Success(URLAnalysis(true, 200, data)))
        }
      case Failure(t) => Future.successful(Failure[URLAnalysis](t))
    }
  }

}

object URLAnalyzeService {
  val STATUS_UNDEFINED = -1
  val UNDEFINED = "Undefined"
}

