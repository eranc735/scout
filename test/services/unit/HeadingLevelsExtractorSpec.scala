package services.unit

import extractors.HeadingsLevelCountersExtractor
import org.jsoup.nodes._
import org.jsoup.parser.Tag
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSClient
import play.api.mvc.Results

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

import collection.JavaConverters._

/**
  * Created by ERAN on 10/15/2017.
  */
class HeadingLevelsExtractorSpec extends FlatSpec with Results with MockitoSugar{

  implicit val wsClient: WSClient = mock[WSClient]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val extractor = HeadingsLevelCountersExtractor
  val doc = mock[Document]

  "Headings counters" should "match number of Headings elements" in {
    val attr: Attribute = new Attribute("title", "page title")
    val attrs: Attributes = new Attributes
    attrs.put(attr)
    val h1Elements = List(new Element(Tag.valueOf("h1"), "", new Attributes), new Element(Tag.valueOf("h1"), "", attrs))
    val h2Elements = List(new Element(Tag.valueOf("h2"), "", new Attributes))
    val h4Elements = List(new Element(Tag.valueOf("h4"), "", new Attributes), new Element(Tag.valueOf("h4"), "", attrs))
    val h5Elements = List(new Element(Tag.valueOf("h5"), "", new Attributes))
    val h6Elements = List(new Element(Tag.valueOf("h6"), "", new Attributes), new Element(Tag.valueOf("h6"), "", attrs))
    val elements = h1Elements ++ h2Elements  ++ h4Elements ++ h5Elements ++ h6Elements
    when(doc.select("h1,h2,h3,h4,h5,h6")) thenReturn new Elements(elements.asJava)
    val counters = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(counters match {
      case Some(count) => count == "2,1,0,2,1,2"
      case _ => false
    })
  }

}
