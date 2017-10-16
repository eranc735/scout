package services.unit

import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSClient
import play.api.mvc.Results
import services.TitleExtractor

/**
  * Created by ERAN on 10/16/2017.
  */
class HeadingLevelsSpec extends FlatSpec with Results with MockitoSugar{

  implicit val wsClient: WSClient = mock[WSClient]
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val extractor = TitleExtractor
  val doc = mock[Document]




  "title" should "be the same as doc title if exists" in {
    when(doc.title) thenReturn "mock title"
    val titleF = extractor.extract(doc)
    titleF.map(title => assert(title match {
      case Some(title) if title == "mock title" => true
      case _ => false
    }))
  }

  "title" should "None if not exists" in {
    when(doc.title) thenReturn null
    val titleF = extractor.extract(doc)
    titleF.map(title => assert(title match {
      case None => true
      case _ => false
    }))
  }

}
