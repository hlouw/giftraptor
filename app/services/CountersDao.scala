package services

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONDocument, BSONInteger}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.Future

object CountersDao {

  def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("counters")

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
      collection = collection.name,
      query = query,
      modify = Update(update, true),
      upsert = true)

    ReactiveMongoPlugin.db.command(command) map { maybeDoc =>
      for {
        doc <- maybeDoc
        seq <- doc.getAs[Int]("seq")
      } yield seq
    }
  }

}
