package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import services.UserDao

import scala.concurrent.Future

object UserController extends Controller {

  val invalid_code = "The code you've entered is invalid. Please try again below."
  val no_code = "No code was entered. Please enter a code below."

  case class SecretCode(code: String)

  val codeForm = Form(
    mapping(
      "code" -> text
    )(SecretCode.apply)(SecretCode.unapply)
  )


  def submitSecret = Action.async { implicit request =>
    codeForm.bindFromRequest.fold(
      formWithErrors => Future(Ok(views.html.signin(invalid_code))),
      userData => {
        val code = userData.code
        for {
          optUser <- UserDao.findUserBySecret(code)
        } yield optUser match {
          case Some(user) => Redirect(routes.UserController.viewProfile).withSession("secret" -> user.secret)
          case None => Ok(views.html.signin(invalid_code)).withNewSession
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
          case None => Ok(views.html.signin(invalid_code)).withNewSession
        }

      case None => Future(Ok(views.html.signin(no_code)))
    }

  }

}
