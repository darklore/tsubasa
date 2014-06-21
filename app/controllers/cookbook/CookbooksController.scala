package controllers.cookbook

import models.Cookbook
import play.api.libs.iteratee.Enumerator
import play.api.{Logger, Play}
import play.api.mvc.{ResponseHeader, SimpleResult, Action, Controller}
import scala.sys.process._
import java.io.File

object CookbooksController extends Controller{

  val cookbookDir      = Play.current.path.getPath + "/data/cookbooks"
  val cookbookFilePath = cookbookDir + "/cookbook.tgz"

  def add = Action {
    Ok(views.html.cookbooks.add())
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("cookbook").map { cookbook =>
      cookbook.ref.moveTo(new File(cookbookFilePath), true)
      val decompressCmd = "tar zxfv " + cookbookFilePath + " -C " + cookbookDir
      val logger = ProcessLogger(
        (o: String) => Logger.info("out " + o),
        (e: String) => Logger.warn("err " + e)
      )
      decompressCmd ! logger
      Redirect(routes.CookbooksController.show)
    }.getOrElse {
      Redirect(routes.CookbooksController.add).flashing(
        "error" -> "Missing File"
      )
    }
  }

  def download = Action {
    import scala.concurrent.ExecutionContext.Implicits.global

    val file = new File(cookbookFilePath)
    val fileContent : Enumerator[Array[Byte]] = Enumerator.fromFile(file)

    SimpleResult(
      header = ResponseHeader(200, Map(CONTENT_LENGTH -> file.length.toString)),
      body = fileContent
    )
  }

  def show = Action {
    val cookbookObj = Cookbook.get()
    Ok(views.html.cookbooks.show(cookbookObj))
  }
}
