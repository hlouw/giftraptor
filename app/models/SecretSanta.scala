package models

import models.SantaModel._
import models.UserModel._

case class SecretSanta(
  _id: SantaId = 0,
  name: String,
  description: String,
  graph: Seq[SantaLink] = Seq[SantaLink](),
  giftseq: Seq[UserId] = Seq[UserId]()) {

  def toGraphMap(): Graph = {
    val z = graph.map(_.from) zip graph.map(_.to.toSet)
    z.toMap[UserId, Set[UserId]]
  }

  def members(): Set[UserId] = {
    graph.map(_.from).toSet[UserId]
  }

}

case class SantaLink(from: Int, to: Set[UserId])

object SantaModel {
  import play.api.libs.json.Json

  type SantaId = Int
  type Graph = Map[UserId, Set[UserId]]
  type Path = List[UserId]

  implicit val santaLinkFormat = Json.format[SantaLink]
  implicit val secretSantaFormat = Json.format[SecretSanta]
}
