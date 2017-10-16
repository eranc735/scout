package services.unit

import org.jsoup.nodes.{Document, DocumentType, Node}
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSClient
import play.api.mvc.Results
import services.{HTMLVersionExtractor, TitleExtractor}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

/**
  * Created by ERAN on 10/16/2017.
  */
class HeadingLevelsSpec extends FlatSpec with Results with MockitoSugar{

  implicit val wsClient: WSClient = mock[WSClient]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val extractor = HTMLVersionExtractor
  val doc = mock[Document]

  "version" should "be HTML5" in {
    val documentTypeNode: Node = new DocumentType("html", "", "", "")
    when(doc.childNodes()) thenReturn List(documentTypeNode).asJava
    val version = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(version match {
      case Some(ver) => ver == "HTML5"
      case _ => false
    })
  }

}
