package controllers.cookbook

import play.api.libs.iteratee.Enumerator
import play.api.{Logger, Play}
import play.api.mvc.{ResponseHeader, SimpleResult, Action, Controller}
import java.io.File

object CookbooksController extends Controller{

  val cookbookFilePath = Play.current.path.getPath + "/data/cookbooks/cookbook.gz"

  def add = Action {
    Ok(views.html.cookbooks.add())
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("cookbook").map { cookbook =>
      val contentType = cookbook.contentType
      val cookbookFile = Play.current.path.getPath + "/data/cookbooks/cookbook.gz"
      cookbook.ref.moveTo(new File(cookbookFilePath), true)
      Ok("File Uploaded")
    }.getOrElse {
      Redirect(routes.CookbooksController.add).flashing(
        "error" -> "Missing File"
      )
    }
  }

  def download = Action {
    import scala.concurrent.ExecutionContext.Implicits.global

    val cookbookFile = Play.current.path.getPath + "/data/cookbooks/cookbook.gz"
    val file = new File(cookbookFilePath)
    val fileContent : Enumerator[Array[Byte]] = Enumerator.fromFile(file)

    SimpleResult(
      header = ResponseHeader(200, Map(CONTENT_LENGTH -> file.length.toString)),
      body = fileContent
    )
  }
}
