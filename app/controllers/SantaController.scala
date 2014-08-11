package controllers

import models.Models._
import models.SecretSanta
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import play.api.libs.concurrent.Execution.Implicits.defaultContext

object SantaController extends Controller with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("secretsantas")

  def create = Action { request =>
    val id = 1
    Ok(s"SantaID created: $id")
  }

  def findById(id: SantaId) = Action.async {
    val futureSantas = collection.find(Json.obj("_id" -> id)).cursor[SecretSanta].collect[List]()

    futureSantas.map(santa => Ok(santa.toString))
  }

  def findByMember(id: UserId) = Action.async {
    val query = Json.obj("graph.from" -> id)
    val futureSantas = collection.find(query).cursor[SecretSanta].collect[List]()

    futureSantas.map(santa => Ok(santa.toString))
  }

}
