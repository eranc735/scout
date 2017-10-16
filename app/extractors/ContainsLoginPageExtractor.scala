package extractors

import org.jsoup.nodes.Document
import play.api.libs.ws.WSClient

import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by ERAN on 10/15/2017.
  */

object ContainsLoginPageExtractor extends DocExtractor {

  override def extract(doc: Document)(implicit ws: WSClient, ec: ExecutionContext):Future[Option[String]] = {
    val isLoginExists = !doc.select("input[type=password]").isEmpty
    Future.successful(Option(isLoginExists.toString))
  }

  override def getExtractorKey(): String = {
    "Contains-Login-Page"
  }
}
