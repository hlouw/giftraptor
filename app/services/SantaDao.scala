package services

import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

import models.Models._
import models.SecretSanta

object SantaDao {

  def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("secretsantas")

  def findSantaById(santaId: SantaId): Future[Option[SecretSanta]] = {
    collection.find(Json.obj("_id" -> santaId)).one[SecretSanta]
  }

  def findSantasByUser(userId: UserId): Future[List[SecretSanta]] = {
    collection.find(Json.obj("graph.from" -> userId)).cursor[SecretSanta].collect[List]()
  }
}
