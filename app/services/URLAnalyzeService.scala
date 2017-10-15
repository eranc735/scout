package services

import models.URLAnalysis
import org.apache.commons.validator.routines.UrlValidator
import play.api.libs.ws.WSClient

import scala.concurrent.Future


/**
  * Created by ERAN on 10/14/2017.
  */
class URLAnalyzeService {

  import scala.language.implicitConversions
  def analyze(url: String, extractors: Seq[DocExtractor])(implicit ws: WSClient): Future[URLAnalysis] = { // to use Either in here? or object or error message?
    val urlValidator: UrlValidator = new UrlValidator()
    if(!urlValidator.isValid(url)) {
      return  Future.successful(URLAnalysis(false, Some(500), Seq.empty))
    }
    import org.jsoup.Jsoup
    val doc = Jsoup.connect(url).get
    val extractedData = Future.sequence(extractors.map(extractor => {
      futureMa(extractor.getExtractorKey(), extractor.extract(doc).map(_.getOrElse("Undefined")))
    }))
    extractedData.map(data => URLAnalysis(true, Some(200), data))
  }

  def futureMa(v1:String, v2f: Future[String]) = {
    for {
      v2 <- v2f
    } yield (v1, v2)
  }

}
