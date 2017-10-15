package services.unit

import org.jsoup.nodes.Document
import play.api.mvc.Results
import services.TitleExtractor
import org.scalatest.mockito.MockitoSugar
import play.api.libs.concurrent.Execution.Implicits._
import org.mockito.Mockito._
import org.scalatest._
import play.api.libs.ws.WSClient


/**
  * Created by ERAN on 10/16/2017.
  */
class TitleExtractorSpec extends FlatSpec with Results with MockitoSugar {

  implicit val wsClient: WSClient = mock[WSClient]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val extractor = TitleExtractor
  val docWithTitle = mock[Document]
  val docWithoutTitle = mock[Document]
  when(docWithTitle.title) thenReturn "mock title"
  when(docWithoutTitle.title) thenReturn null


  "title" should "be the same as doc title if exists" in {
    val titleF = extractor.extract(docWithTitle)
    titleF.map(title => assert(title match {
      case Some(title) if title == "mock title" => true
      case _ => false
    }))
  }

  "title" should "None if not exists" in {
    val titleF = extractor.extract(docWithTitle)
    titleF.map(title => assert(title match {
      case None => true
      case _ => false
    }))
  }
}
