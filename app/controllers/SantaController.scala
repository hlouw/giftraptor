package controllers

import controllers.santa.SantaPath
import models.Models._
import models.SecretSanta
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

object SantaController extends Controller with MongoController {

  def secretsantas: JSONCollection = db.collection[JSONCollection]("secretsantas")

  def create = Action { request =>
    val id = 1
    Ok(s"SantaID created: $id")
  }

  def findById(id: SantaId) = Action.async {
    val futureSantas = secretsantas.find(Json.obj("_id" -> id)).cursor[SecretSanta].collect[List]()

    futureSantas.map(santa => Ok(santa.map(_.toGraphMap()).toString))
  }

  def findByMember(id: UserId) = Action.async {
    val query = Json.obj("graph.from" -> id)
    val futureSantas = secretsantas.find(query).cursor[SecretSanta].collect[List]()

    futureSantas.map(santa => Ok(santa.toString))
  }

  /**
   * Generate a valid sequence of gift-giving for the given Secret Santa event.
   *
   * @param id Identifier for the event.
   * @return
   */
  def generate(id: SantaId) = Action.async {
    val futureSanta = secretsantas.find(Json.obj("_id" -> id)).one[SecretSanta]

    val f = for {
      santa <- futureSanta
      s = santa.get
      graph = s.toGraphMap
      sp = new SantaPath(graph)
      goal = graph.keys.head
      solution <- sp.solve(goal)
    } yield solution

    f map { solution =>
      Ok(solution.toString)
    }
  }

}
