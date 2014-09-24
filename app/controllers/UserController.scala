package controllers

import play.api.Logger
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import services.UserDao

import scala.concurrent.Future

object UserController extends Controller with MongoController {

  case class SecretCode(code: String)

  val codeForm = Form(
    mapping(
      "code" -> text
    )(SecretCode.apply)(SecretCode.unapply)
  )


  def submitSecret = Action.async { implicit request =>
    codeForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest("Invalid code")),
      userData => {
        val code = userData.code
        for {
          optUser <- UserDao.findUserBySecret(code)
        } yield optUser match {
          case Some(user) => Redirect(routes.UserController.viewProfile).withSession("secret" -> user.secret)
          case None => BadRequest(s"Invalid secret").withNewSession
        }
      }
    )
  }

  def viewProfile = Action.async { implicit request =>
    request.session.get("secret") match {
      case Some(secret) =>
        for {
          optUser <- UserDao.findUserBySecret(secret)
        } yield optUser match {
          case Some(user) => Ok(views.html.profile(user.name)).withSession("secret" -> user.secret)
          case None => BadRequest(s"Invalid secret code").withNewSession
        }

      case None => Future(BadRequest("No secret code entered"))
    }

  }

}
