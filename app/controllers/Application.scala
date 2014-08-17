package controllers

import models.SecretSanta
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def admin = TODO

}