package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def profile = Action {
    Ok(views.html.profile("hlouw"))
  }

  def admin = Action {
    Ok(views.html.admin("hlouw"))
  }

}