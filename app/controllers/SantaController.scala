package controllers

import controllers.santa.SantaSolver
import models.Models._
import models.{User, SecretSanta}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future
import scala.util.Random
import scala.util.{Success, Failure}

object SantaController extends Controller with MongoController {

  def secretsantas: JSONCollection = db.collection[JSONCollection]("secretsantas")
  def users: JSONCollection = db.collection[JSONCollection]("users")

  def create = Action { request =>
    val id = 1
    Ok(s"SantaID created: $id")
  }

  def findById(id: SantaId) = Action.async {
    val futureSantas = secretsantas.find(Json.obj("_id" -> id)).cursor[SecretSanta].collect[List]()

    futureSantas.map(santa => Ok(santa.map(_.toGraphMap()).toString))
  }

  /**
   * Generate and persist a valid sequence of gift-giving for the given Secret Santa event.
   * Skip if a sequence already exists in DB.
   *
   * @param id Identifier for the event.
   * @return
   */
  def generate(id: SantaId) = Action.async {
    val selector = Json.obj("_id" -> id, "giftseq" -> Json.obj("$exists" -> false))
    val futureSanta = secretsantas.find(selector).one[SecretSanta]

    val giftseq: Future[Option[List[UserId]]] = futureSanta flatMap {
      case Some(santa) =>
        val graph = santa.toGraphMap()
        val solver = new SantaSolver(graph)
        val goal = graph.keys.head
        solver.solve(goal) map { solutions =>
          Some(solutions.toList(Random.nextInt(solutions.size)))
        }

      case None =>
        Future(None)
    }

    giftseq map {
      case Some(solution) =>
        val update = Json.obj("$set" -> Json.obj("giftseq" -> solution.tail))
        val lastError = secretsantas.update(selector, update)
        Ok(s"Solution: $solution, LastError: $lastError")

      case None =>
        Ok(s"No update")
    }
  }

  def getNamesFromDb(members: Set[UserId]): Future[Map[UserId, String]] = {
    val query = Json.obj("_id" -> Json.obj("$in" -> members))
    val futureMembers = users.find(query).cursor[User].collect[List]()

    futureMembers map { members =>
      val m = members.map(_._id) zip members.map(_.name)
      m.toMap[UserId, String]
    }
  }

}
