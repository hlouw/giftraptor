package models

import models.Models.UserId
import models.Models.SantaId

case class User(
  _id: UserId,
  name: String)

case class SecretSanta(
  _id: SantaId,
  name: String,
  graph: Seq[SantaLink]) {

  def toGraphMap() = {
    val z = graph.map(_.from) zip graph.map(_.to.toSet)
    z.toMap[Int, Set[Int]]
  }
}

case class SantaLink(from: Int, to: Set[UserId])

object Models {
  import play.api.libs.json.Json

  type UserId = Int
  type SantaId = Int

  implicit val userFormat = Json.format[User]
  implicit val santaLinkFormat = Json.format[SantaLink]
  implicit val secretSantaFormat = Json.format[SecretSanta]
}
