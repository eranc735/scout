package services.unit

import java.net.URL

import extractors.HLinksCounterExtractor
import org.jsoup.nodes.{Attribute, Attributes, Document, Element}
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

class HLinksCounterSpec extends FlatSpec with Results with MockitoSugar{

  implicit val wsClient: WSClient = mock[WSClient]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val extractor = HLinksCounterExtractor
  val doc = mock[Document]

  "links counters" should "match number of external and internal links elments" in {
    val internalAttr1: Attribute = new Attribute("abs:href", "http://www.domain.com/index")
    val internalAttr2: Attribute = new Attribute("abs:href", "http://www.domain.com/search")
    val externalAttr1: Attribute = new Attribute("abs:href", "http://www.google.com/index")
    val internalAttrs1: Attributes = new Attributes
    val internalAttrs2: Attributes = new Attributes
    val externalAttrs1: Attributes = new Attributes
    internalAttrs1.put(internalAttr1)
    internalAttrs2.put(internalAttr2)
    externalAttrs1.put(externalAttr1)
    val internalLinks = List(new Element(Tag.valueOf("a[href]"), "", internalAttrs1), new Element(Tag.valueOf("a[href]"), "", internalAttrs2))
    val externalLinks = List(new Element(Tag.valueOf("a[href]"), "",externalAttrs1))
    val elements = internalLinks ++ externalLinks
    when(doc.select("a[href]")) thenReturn new Elements(elements.asJava)
    when(doc.baseUri()) thenReturn "https://www.domain.com"
    val counters = Await.result(extractor.extract(doc), Duration(1, SECONDS))
    assert(counters match {
      case Some(count) => count == "2 Internal links 1 External links"
      case _ => false
    })
  }

}
