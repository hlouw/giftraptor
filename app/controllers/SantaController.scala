package controllers

import models.SantaModel._
import models.UserModel._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import services.{SantaDao, UserDao}

import scala.concurrent.Future

/**
 *
 */
object SantaController extends Controller {


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
      santas <- SantaDao.findSantasByUser(userId)
    } yield Ok(Json.toJson(santas)) as JSON
  }

  /**
   * Find the person for which the current user must buy a gift.
   *
   * @param santaId The secret santa event in question.
   * @return
   */
  def findGiftee(santaId: SantaId) = Action.async { request =>
    val secret = request.session.get("secret")

    for {
      optUserId <- getUserIdForSecret(secret)
      userId = optUserId.get if optUserId.isDefined
      santa <- SantaDao.findSantaById(santaId)
      giftseq = santa.get.giftseq if santa.isDefined
      giftee <- UserDao.findUserById(getGiftee(userId, giftseq))
      gifteeName = giftee.get.name if giftee.isDefined
    } yield Ok(gifteeName) as JSON
  }

  private def getUserIdForSecret(secret: Option[String]): Future[Option[UserId]] = {
    val futureUser = secret match {
      case Some(key) => UserDao.findUserBySecret(key)
      case None => Future(None)
    }

    futureUser map {
      case Some(user) => Some(user._id)
      case None => None
    }
  }

  private def getGiftee(gifter: UserId, giftseq: Seq[UserId]): UserId = {
    giftseq((giftseq.indexOf(gifter)+1) % giftseq.length)
  }

}
