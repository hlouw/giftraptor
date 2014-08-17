package controllers

import models.Models._
import models.SecretSanta
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object ProfileController extends Controller with MongoController {
  def secretsantas: JSONCollection = db.collection[JSONCollection]("secretsantas")
  def users: JSONCollection = db.collection[JSONCollection]("users")

  def findSantas(user: String) = Action.async {
    val userId = 1
    val query = Json.obj("graph.from" -> userId)
    val futureSantas = secretsantas.find(query).cursor[SecretSanta].collect[List]()

    futureSantas.map(santas => Ok(views.html.profile(santas)))
  }

}
