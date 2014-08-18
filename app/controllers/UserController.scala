package controllers

import models.Models._
import models.{User, SecretSanta}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object UserController extends Controller with MongoController {
  def secretsantas: JSONCollection = db.collection[JSONCollection]("secretsantas")
  def users: JSONCollection = db.collection[JSONCollection]("users")

  def findSantas(user: String) = Action.async {
    val userId = 1
    val query = Json.obj("graph.from" -> userId)
    val futureSantas = secretsantas.find(query).cursor[SecretSanta].collect[List]()

    futureSantas.map(santas => Ok(views.html.profile(user, santas)))
  }

  private def getNamesFromDb(members: Set[UserId]): Future[Map[UserId, String]] = {
    val query = Json.obj("_id" -> Json.obj("$in" -> members))
    val futureMembers = users.find(query).cursor[User].collect[List]()

    futureMembers map { members =>
      val m = members.map(_._id) zip members.map(_.name)
      m.toMap[UserId, String]
    }
  }

}
