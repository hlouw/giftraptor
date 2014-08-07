package controllers

import play.api._
import play.api.mvc._

import scala.concurrent.Promise
import scala.util.Success

import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  val p = Promise[String]()

  def index = Action {
    Ok(views.html.index())
  }

  def profile(user: String) = Action {
    Ok(views.html.profile(user))
  }

  def admin = Action {
    Ok(views.html.admin(s"Administrator"))
  }

}