package services

import models.URLAnalysis
import org.apache.commons.validator.routines.UrlValidator
import play.Logger
import play.api.libs.ws.WSClient
import org.jsoup.Jsoup
import org.jsoup.Connection.Method.GET

import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by ERAN on 10/14/2017.
  */
trait URLAnalyzeService {

  implicit val ec: ExecutionContext

  import scala.language.implicitConversions

  val STATUS_UNDEFINED = -1

  def analyze(url: String, extractors: Seq[DocExtractor])(implicit ws: WSClient): Future[URLAnalysis] = {
    val urlValidator: UrlValidator = new UrlValidator()
    if(!urlValidator.isValid(url)) {
      return  Future.successful(URLAnalysis(false, STATUS_UNDEFINED, Seq.empty))
    }
    val doc = Jsoup.connect(url).method(GET).timeout(20*1000).get
    val extractedData = Future.sequence(extractors.map(extractor => {
      elementFuture(extractor.getExtractorKey(), extractor.extract(doc).map(_.getOrElse("Undefined")))
    }))
    extractedData.map(data => URLAnalysis(true, 200, data))
  }

  implicit def elementFuture(key:String, value: Future[String]): Future[(String, String)] = {
    for {
      v <- value
    } yield (key, v)
  }

}
