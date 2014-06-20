package controllers.server

import domain.service.server.ServerInitializers
import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import models.{ Server, Servers, ServerForm }

object ServersController extends Controller {

  val serverForm = Form(
    mapping(
      "privateIpAddr" -> text
    )(ServerForm.apply)(ServerForm.unapply)
  )

  def index = Action {
    Ok(views.html.servers.index(Servers.all()))
  }

  def create = Action { implicit request =>
    val form = serverForm.bindFromRequest.get
    val server = Server(0, form.privateIpAddr)
    Servers.insert(server)
    ServerInitializers.getInitializer(server).start()
    Redirect(routes.ServersController.index())
  }

  def add = Action {
    Ok(views.html.servers.add(serverForm))
  }

  def show(id: Long) = Action {
    val server = Servers.findById(id)
    Ok(views.html.servers.show(server))
  }

  def edit(id: Long) = Action {
    val server = Servers.findById(id)
    val form = ServerForm(server.privateIpAddr)
    Ok(views.html.servers.edit(serverForm.fill(form), id))
  }

  def update(id: Long) = Action { implicit request =>
    val form = serverForm.bindFromRequest.get
    val server = Server(id, form.privateIpAddr)
    Servers.update(server)
    Redirect(routes.ServersController.show(id))
  }
  def update_post(id: Long) = update(id)

  def delete(id: Long) = Action {
    Servers.delete(id)
    Redirect(routes.ServersController.index())
  }
  def delete_get(id: Long) = delete(id)

}
