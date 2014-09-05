package controllers

import controllers.UserController._
import controllers.santa.SantaSolver
import models.Models._
import models.{SantaLink, SecretSanta, User}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsSuccess, JsError, Json}
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands._

import scala.concurrent.Future
import scala.util.{Random, Failure, Success}

object SantaController extends Controller with MongoController {

  def secretsantas = db.collection[JSONCollection]("secretsantas")
  def users = db.collection[JSONCollection]("users")
  def counters = db.collection[JSONCollection]("counters")

  /**
   * Persist and return the next sequence number for the given sequence name.
   *
   * @param name Sequence name to tick over.
   * @return Next sequence number.
   */
  def nextSeq(name: String): Future[Option[Int]] = {
    val query = BSONDocument("_id" -> name)
    val update = BSONDocument("$inc" -> BSONDocument("seq" -> BSONInteger(1)))

    val command = FindAndModify(
      collection = counters.name,
      query = query,
      modify = Update(update, true),
      upsert = true)

    db.command(command) map { maybeDoc =>
      for {
        doc <- maybeDoc
        seq <- doc.getAs[Int]("seq")
      } yield seq
    }
  }

  /**
   * Create a new Secret Santa occasion.
   * @return
   */
  def create = Action.async(BodyParsers.parse.json) { request =>
    val futureId = nextSeq(secretsantas.name)

    futureId flatMap {
      case Some(seq) =>
        val json = request.body
        val name = (json \ "name").as[String]
        val description = (json \ "description").as[String]
        val santa = SecretSanta(_id = seq, name = name, description = description)
        secretsantas.insert(santa) map { lastError =>
          Ok(s"Santa created: $santa")
        }
      case None =>
        Future(BadRequest(s"Santa not created"))
    }
  }

  /**
   *
   * @param id
   * @return
   */
  def findById(id: SantaId) = Action.async {
    val futureSantas = secretsantas.find(Json.obj("_id" -> id)).cursor[SecretSanta].collect[List]()

    futureSantas map { santa =>
      Ok(santa.map(_.toGraphMap()).toString) as JSON
    }
  }

  /**
   * Find all santas for the current session user.
   *
   * @return
   */
  def findSantas = Action.async { request =>
    val secret = request.session.get("secret")

    for {
      optUserId <- getUserIdForSecret(secret)
      userId = optUserId.get if optUserId.isDefined
      query = Json.obj("graph.from" -> userId)
      santas <- secretsantas.find(query).cursor[SecretSanta].collect[List]()
    } yield Ok(Json.toJson(santas)) as JSON
  }

  private def getUserIdForSecret(secret: Option[String]): Future[Option[UserId]] = {
    val futureUser = secret match {
      case Some(key) =>
        val query = Json.obj("secret" -> secret);
        users.find(query).one[User]

      case None =>
        Future(None)
    }

    futureUser map {
      case Some(user) => Some(user._id)
      case None => None
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

    val giftseq: Future[Option[Path]] = futureSanta flatMap {
      case Some(santa) =>
        val graph = santa.toGraphMap()
        val goal = graph.keys.head
        SantaSolver(graph).solve(goal)

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
