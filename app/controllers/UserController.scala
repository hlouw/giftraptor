package controllers

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import services.UserDao

object UserController extends Controller with MongoController {

  def viewProfile = Action.async { request =>
    val secret = request.cookies.get("secret") map { cookie => cookie.value.trim }

    Logger.info(s"User secret: $secret")

    for {
      optUser <- UserDao.findUserBySecret(secret.get)
      result = optUser match {
        case Some(user) => Ok(views.html.profile(user.name)).withSession("secret" -> user.secret)
        case None => BadRequest(s"Invalid secret").withNewSession
      }
    } yield result
  }

}
