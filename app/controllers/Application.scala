package controllers

import models.SecretSanta
import play.api.mvc._

object Application extends Controller {

  def landing = Action {
    Ok(views.html.index())
  }

}