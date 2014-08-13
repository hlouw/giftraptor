package controllers

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import controllers.services.PathRoot.FindSolution
import controllers.services.{PathRoot, SantaPath}
import models.Models._
import models.SecretSanta
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import play.api.Play.current

object SantaController extends Controller with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("secretsantas")

  def create = Action { request =>
    val id = 1
    Ok(s"SantaID created: $id")
  }

  def findById(id: SantaId) = Action.async {
    val futureSantas = collection.find(Json.obj("_id" -> id)).cursor[SecretSanta].collect[List]()

    futureSantas.map(santa => Ok(santa.toString))
  }

  def findByMember(id: UserId) = Action.async {
    val query = Json.obj("graph.from" -> id)
    val futureSantas = collection.find(query).cursor[SecretSanta].collect[List]()

    futureSantas.map(santa => Ok(santa.toString))
  }

  def generate(id: SantaId) = Action.async {
    val graph = Map[UserId, Set[UserId]](
      1 -> Set(2, 3, 4, 5),
      2 -> Set(1, 3, 4, 5),
      3 -> Set(1, 2, 4, 5),
      4 -> Set(1, 2, 3, 5),
      5 -> Set(1, 2, 3, 4)
    )
    val goal = 1

    implicit val timeout = Timeout(5 seconds)

    val actor = Akka.system.actorOf(Props[PathRoot], id.toString)
    val futurePath = ask(actor,  FindSolution(graph, goal)).mapTo[List[UserId]]
    futurePath.map(path => Ok(path.toString))
  }

}
