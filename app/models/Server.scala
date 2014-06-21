package models

import play.api.db.DB
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import scala.language.postfixOps

case class Server(id: Long, ipAddr: String)

object Servers {

  val database = Database.forDataSource(DB.getDataSource())

  class ServerTable(tag: Tag) extends Table[Server](tag, "Server") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def privateIpAddr = column[String]("PRIVATE_IP_ADDR", O.NotNull)
    def * = (id, privateIpAddr) <> (Server.tupled, Server.unapply)
  }

  // query object
  val servers = TableQuery[ServerTable]

  def all(): List[Server] = database.withTransaction { implicit session: Session =>
    servers.list()
  }

  def insert(s: Server) = database.withTransaction { implicit session: Session =>
    servers.insert(s)
  }

  /** ID検索 */
  def findById(id: Long): Server = database.withTransaction { implicit session: Session =>
    servers.filter(_.id === id).first
  }

  def update(s: Server) = database.withTransaction { implicit session: Session =>
    servers.filter(_.id === s.id).update(s)
  }

  def delete(id: Long) = database.withTransaction { implicit session: Session =>
    servers.where(_.id === id).delete
  }
}