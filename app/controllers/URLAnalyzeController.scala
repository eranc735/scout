package controllers

import javax.inject.Inject

import models.URLInfo
import play.Logger
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.ws.WSClient
import extractors._
import services.URLAnalyzeService

import scala.concurrent.Future
import scala.util.{Failure, Success}

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
    LinkStatusExtractor)

  val urlAnalyzerform = Form(
    mapping( "url" -> text)(URLInfo.apply)(URLInfo.unapply)
  )

  def index = Action {
    Ok(views.html.index())
  }

  def submit = Action.async { implicit request =>
    urlAnalyzerform.bindFromRequest.fold(
      formWithErrors => {
          Logger.info("cant bind form for request %s".format(request))
          Future.successful(BadRequest("Cant parse form")
        )
      },
    url => {
      val analysisResult = analyze(url.url, extractors)
        for {
          analysis <- analysisResult
        } yield {
          analysis match {
            case Success(analysis) => {
              if(!analysis.isValid) {
                BadRequest("Cant Parse URL: %s".format(url.url))
              }
              else if(analysis.status != OK) {
                Status(analysis.status)("Error occured while trying to analyze %s".format(url.url))
              }
              Ok(views.html.analysis(analysis.data))
            }
            case Failure(t) => {
              BadRequest("Error occurred during url analysis. Error msg: %s".format(t.getMessage))
            }
          }

         }
     })
  }
}