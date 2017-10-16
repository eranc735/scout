package services.unit

import org.jsoup.nodes.{Document, DocumentType, Node}
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSClient
import play.api.mvc.Results
import services.{HTMLVersionExtractor, HeadingsLevelCountersExtractor}

import collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

/**
  * Created by ERAN on 10/16/2017.
  */
class HTMLVersionExtractorSpec extends FlatSpec with Results with MockitoSugar {

  implicit val wsClient: WSClient = mock[WSClient]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val extractor = HeadingsLevelCountersExtractor
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

  "version" should "be XHTML 1.0" in {
    val documentTypeNode: Node = new DocumentType("html", "-//W3C//DTD XHTML 1.0//EN", "", "")
    when(doc.childNodes()) thenReturn List(documentTypeNode).asJava
    val version = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(version match {
      case Some(ver) => ver == "XHTML 1.0"
      case _ => false
    })
  }

  "version" should "be HTML 4.01" in {
    val documentTypeNode: Node = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "")
    when(doc.childNodes()) thenReturn List(documentTypeNode).asJava
    val version = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(version match {
      case Some(ver) => ver == "HTML 4.01"
      case _ => false
    })
  }

  "version" should "be Undefined when fields are empty" in {
    val documentTypeNode: Node = new DocumentType("", "", "", "")
    when(doc.childNodes()) thenReturn List(documentTypeNode).asJava
    val version = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(version match {
      case None => true
      case _ => false
    })
  }

  "version" should "be Undefined when regex doesn't match any version" in {
    val documentTypeNode: Node = new DocumentType("html", "-//W3C //EN", "", "")
    when(doc.childNodes()) thenReturn List(documentTypeNode).asJava
    val version = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(version match {
      case None => true
      case _ => false
    })
  }

}

