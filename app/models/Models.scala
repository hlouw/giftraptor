package models

import models.Models._

case class User(
  _id: UserId,
  name: String)

case class SecretSanta(
  _id: SantaId,
  name: String,
  description: String,
  graph: Seq[SantaLink]) {

  def toGraphMap(): Graph = {
    val z = graph.map(_.from) zip graph.map(_.to.toSet)
    z.toMap[UserId, Set[UserId]]
  }

  def members(): Set[UserId] = {
    graph.map(_.from).toSet[UserId]
  }
}

case class SantaLink(from: Int, to: Set[UserId])

object Models {
  import play.api.libs.json.Json

  type UserId = Int
  type SantaId = Int
  type Graph = Map[UserId, Set[UserId]]
  type Path = List[UserId]

  implicit val userFormat = Json.format[User]
  implicit val santaLinkFormat = Json.format[SantaLink]
  implicit val secretSantaFormat = Json.format[SecretSanta]
}
