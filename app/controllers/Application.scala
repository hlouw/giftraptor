package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def profile(user: String) = Action {
    Ok(views.html.profile())
  }

  def admin = Action {
    Ok(views.html.admin())
  }

}