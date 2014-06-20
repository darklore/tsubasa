package domain.service.server

import akka.actor.{Actor, Props}
import models.Server
import play.api.{Play, Logger}
import play.api.Play.current
import play.api.libs.concurrent.Akka

class ServerInitializer(server: Server) {

  val ref = Akka.system.actorOf(Props(new ServerActor))

  def start() = {
    Logger.info(server.privateIpAddr)
    ref ! server.privateIpAddr
  }

  class ServerActor extends Actor {
    import scala.sys.process._

    val logger = ProcessLogger(
      (o: String) => Logger.info("[" + server.privateIpAddr + "]" + " out " + o),
      (e: String) => Logger.warn("[" + server.privateIpAddr + "]" + " err " + e)
    )

    def receive = {
      case s: String =>
        val rootPath = Play.current.path.getPath()
        val sshKeyPath = rootPath + "/conf/ssh_keys/id_rsa"

        val cmd = "/usr/bin/knife solo prepare " + s + " -x vagrant -i " + sshKeyPath + " --bootstrap-version 11.12.0"
        Logger.info("Start: " + cmd)
        cmd ! logger
      case _ =>
        Logger.warn("Not string.")
    }
  }
}
