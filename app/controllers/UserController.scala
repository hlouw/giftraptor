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

  def viewProfile(user: String) = Action {
    Ok(views.html.profile(user))
  }

  def viewSecretProfile(key: String) = Action.async {
    val query = Json.obj("secret" -> key)
    val futureUser = users.find(query).one[User]

    for {
      optUser <- futureUser
      result = optUser match {
        case Some(user) => Ok(views.html.profile(user.name)).withSession("secret" -> key)
        case None => BadRequest(s"Invalid secret").withNewSession
      }
    } yield result
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
