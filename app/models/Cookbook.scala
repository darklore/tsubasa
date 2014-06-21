package models

import play.api.Play
import play.api.libs.json.{JsObject, Json}

import scalax.file.Path

case class Cookbook(name: String, version: String, recipes: List[String])

object Cookbook {
  val cookbookDir      = Play.current.path.getPath + "/data/cookbooks/cookbooks"

  def get() = {
    val metadataPath = Path("data", "cookbooks", "cookbooks", "serf-cookbook", "metadata.json")
    val json = Json.parse(metadataPath.string)
    val name = (json \ "name").as[String]
    val recipes = (json \ "recipes").as[JsObject].fieldSet.map { s => s._1 }.toList
    val version = (json \ "version").as[String]
    Cookbook(name, version, recipes)
  }
}