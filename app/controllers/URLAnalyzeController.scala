package controllers

import javax.inject.Inject

import models.URLINFO
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.ws.WSClient
import services._

/**
  * Created by ERAN on 10/14/2017.
  */

class URLAnalyzeController @Inject()(implicit ws: WSClient) extends Controller with URLAnalyzeService{

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global


  val extractors: Seq[DocExtractor] = Seq(
    HTMLVersionExtractor,
    HeadingsLevelCountersExtractor,
    TitleExtractor,
    HLinksCounterExtractor,
    ContainsLoginPageExtractor,
    LinksValidationExtractor)

  val urlAnalyzerform = Form(
    mapping( "url" -> text)(URLINFO.apply)(URLINFO.unapply)
  )

  def index = Action {
    Ok(views.html.index())
  }

  def submit = Action.async { implicit request =>
    val url = urlAnalyzerform.bindFromRequest.get
    val analysisResult = analyze(url.url, extractors)
    for {
      analysis <- analysisResult
    } yield {
      if(analysis.isValid || analysis.status.get != 200) {
        Forbidden("You're not allowed to access this resource.")
      }
      Ok(views.html.analysis(analysis.data))
    }
  }
}