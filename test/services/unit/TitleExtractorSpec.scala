package services.unit

import extractors.TitleExtractor
import org.jsoup.nodes.Document
import play.api.mvc.Results
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatest._
import play.api.libs.ws.WSClient

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}


/**
  * Created by ERAN on 10/15/2017.
  */
class TitleExtractorSpec extends FlatSpec with Results with MockitoSugar {

  implicit val wsClient: WSClient = mock[WSClient]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val extractor = TitleExtractor
  val doc = mock[Document]



  "title" should "be the same as doc title if exists" in {
    when(doc.title) thenReturn "mock title"
    val title = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(title match {
      case Some(t) => t == "mock title"
      case _ => false
    })
  }

  "title" should "None if not exists" in {
    when(doc.title) thenReturn null
    val title = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(title match {
      case None => true
      case _ => false
    })
  }



}
