package models

import models.UserModel._

case class User(_id: UserId, name: String, secret: String)

object UserModel {
  import play.api.libs.json.Json

  type UserId = Int

  implicit val userFormat = Json.format[User]
}