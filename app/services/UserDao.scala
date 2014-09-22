package services

import play.api.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

import models.UserModel._
import models.User

object UserDao {

  private def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")

  def findUserBySecret(secret: String): Future[Option[User]] = {
    collection.find(Json.obj("secret" -> secret)).one[User]
  }

  def findUserById(userId: UserId): Future[Option[User]] = {
    Logger.info(s"Finding user: $userId")
    collection.find(Json.obj("_id" -> userId)).one[User]
  }

  def findNames(members: Set[UserId]): Future[Map[UserId, String]] = {
    val query = Json.obj("_id" -> Json.obj("$in" -> members))
    val futureMembers = collection.find(query).cursor[User].collect[List]()

    futureMembers map { members =>
      val m = members.map(_._id) zip members.map(_.name)
      m.toMap[UserId, String]
    }
  }

}
