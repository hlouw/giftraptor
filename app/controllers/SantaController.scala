package controllers

import controllers.santa.SantaSolver
import models.Models._
import models.{SantaLink, SecretSanta}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsSuccess, JsError, Json}
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future
import scala.util.Random

object SantaController extends Controller with MongoController {

  def secretsantas = db.collection[JSONCollection]("secretsantas")
  def users = db.collection[JSONCollection]("users")
  def counters = db.collection[JSONCollection]("counters")

  /**
   * Create a new Secret Santa occasion.
   * @return
   */
  def create = Action(BodyParsers.parse.json) { request =>
    val json = request.body
    val name = (json \ "name").as[String]
    val description = (json \ "description").as[String]
    val santa = SecretSanta(name = name, description = description)

    val id = 1
    val newSanta = santa.copy(_id = id)
    Ok(s"Santa created: $newSanta")
  }

  def findById(id: SantaId) = Action.async {
    val futureSantas = secretsantas.find(Json.obj("_id" -> id)).cursor[SecretSanta].collect[List]()

    futureSantas.map { santa =>
      Ok(santa.map(_.toGraphMap()).toString) as JSON
    }
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

  def addMember(santaId: SantaId, userId: UserId) = Action.async {
    val selector = Json.obj("_id" -> santaId)
    val futureSanta = secretsantas.find(selector).one[SecretSanta]

    val lastError = for {
      optSanta <- futureSanta
      if optSanta.isDefined
      santa = optSanta.get
      newSanta = addMemberToGraph(santa, userId)
      update = Json.obj("$set" -> Json.obj("graph" -> newSanta.graph))
      lastError <- secretsantas.update(selector, update)
    } yield lastError

    lastError map { le =>
      Ok(le.toString)
    }
  }

  def declique(santaId: SantaId) = Action.async(BodyParsers.parse.json) { request =>
    val members = request.body.validate[List[UserId]].get

    val selector = Json.obj("_id" -> santaId)
    val futureSanta = secretsantas.find(selector).one[SecretSanta]

    val lastError = for {
      optSanta <- futureSanta
      if optSanta.isDefined
      santa = optSanta.get
      newSanta = decliqueMembers(santa, members)
      update = Json.obj("$set" -> Json.obj("graph" -> newSanta.graph))
      lastError <- secretsantas.update(selector, update)
    } yield lastError

    lastError map { le =>
      Ok(le.toString)
    }
  }

  private def decliqueMembers(santa: SecretSanta, members: List[UserId]) = {
    val newGraph = santa.graph map { link =>
      if (members.contains(link.from))
        link.copy(to = link.to filter {
          !members.contains(_)
        } )
      else link
    }

    santa.copy(graph = newGraph)
  }

  private def addMemberToGraph(santa: SecretSanta, newMember: UserId): SecretSanta = {
    val otherMembers = santa.members filter { _ != newMember }
    val newLink = SantaLink(from = newMember, to = otherMembers)
    val newGraph = santa.graph map { link =>
      link.copy(to = link.to + newMember)
    }

    santa.copy(graph = newLink +: newGraph)
  }

}
