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
    Logger.info(server.ipAddr)
    ref ! new InitializeCommand
  }

  def install(recipeName: String) {
    Logger.info("Install")
    ref ! new InstallCommand(recipeName)
  }

  class ServerActor extends Actor {
    import scala.sys.process._

    val logger = ProcessLogger(
      (o: String) => Logger.info("[" + server.ipAddr + "]" + " out " + o),
      (e: String) => Logger.warn("[" + server.ipAddr + "]" + " err " + e)
    )

    def receive = {
      case _: InitializeCommand =>
        val cmd = Seq("/usr/bin/knife", "solo","prepare", server.ipAddr, "-x", "vagrant", "-i", sshKeyPath, "--bootstrap-version", "11.12.0")
        Logger.info("Start: " + cmd)
        cmd ! logger

      case installCmd: InstallCommand =>
        val cookbookDir = Play.current.path.getPath + "/data/cookbooks/cookbooks"

        // sync cookbook
        val rsyncCmd = Seq("/usr/bin/rsync", "-a", "--delete", "-e", "ssh -i " + sshKeyPath, cookbookDir + "/", "vagrant@" + server.ipAddr + ":/tmp/cookbooks")
        Logger.info("Start: " + rsyncCmd)
        rsyncCmd ! logger

        // set cookbook path
        val cookbookPathCmd = Seq("ssh", "vagrant@" + server.ipAddr, "-i", sshKeyPath, "echo \"cookbook_path '/tmp/cookbooks'\" > /tmp/solo.rb")
        Logger.info("start: " + cookbookPathCmd)
        cookbookPathCmd ! logger

        // execute chef-solo
        val chefSoloCmd = Seq("ssh", "vagrant@" + server.ipAddr, "-i", sshKeyPath,
          "-t", "sudo chef-solo -c /tmp/solo.rb -o \"recipe[" + installCmd.recipeName + "]\" -l debug")
        Logger.info("Start: " + chefSoloCmd)
        chefSoloCmd ! logger
      case _ =>
        Logger.warn("Unknown command.")
    }
  }
}
