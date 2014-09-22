package controllers

import play.api.mvc._

object Application extends Controller {

  def landing = Action {
    Ok(views.html.index())
  }

}