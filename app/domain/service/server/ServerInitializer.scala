package domain.service.server

import akka.actor.{Actor, Props}
import models.Server
import play.api.{Play, Logger}
import play.api.Play.current
import play.api.libs.concurrent.Akka

class ServerInitializer(server: Server) {

  val ref = Akka.system.actorOf(Props(new ServerActor))

  val sshKeyPath = Play.current.path.getPath() + "/conf/ssh_keys/id_rsa"

  case class InitializeCommand()
  case class InstallCommand(recipeName: String)

  def start() = {
    Logger.info(server.privateIpAddr)
    ref ! new InitializeCommand
  }

  def install(recipeName: String) {
    Logger.info("Install")
    ref ! new InstallCommand(recipeName)
  }

  class ServerActor extends Actor {
    import scala.sys.process._

    val logger = ProcessLogger(
      (o: String) => Logger.info("[" + server.privateIpAddr + "]" + " out " + o),
      (e: String) => Logger.warn("[" + server.privateIpAddr + "]" + " err " + e)
    )

    def receive = {
      case _: InitializeCommand =>
        val cmd = "/usr/bin/knife solo prepare " + server.privateIpAddr + " -x vagrant -i " + sshKeyPath + " --bootstrap-version 11.12.0"
        Logger.info("Start: " + cmd)
        cmd ! logger

      case installCmd: InstallCommand =>
        val cookbookDir = Play.current.path.getPath + "/data/cookbooks/cookbooks"
        // delete previous cookbook
        val rmCmd = "ssh -l vagrant -i " + sshKeyPath + " " + server.privateIpAddr +
          " rm -r /tmp/cookbooks"
        Logger.info("Start: " + rmCmd)
        rmCmd ! logger

        //upload cookbook
        val uploadCmd = "/usr/bin/scp -i " + sshKeyPath + " -r " +
          cookbookDir + " vagrant@" + server.privateIpAddr + ":/tmp/cookbooks"
        Logger.info("Start: " + uploadCmd)
        uploadCmd ! logger

        // execute chef-solo
        val chefSoloCmd = "ssh -l vagrant -i " + sshKeyPath + " " + server.privateIpAddr +
          " -t sudo chef-solo -c /tmp/solo.rb -o \"recipe[" + installCmd.recipeName + "]\" -l debug"
        Logger.info("Start: " + chefSoloCmd)
        chefSoloCmd ! logger
      case _ =>
        Logger.warn("Unknown command.")
    }
  }
}
