package controllers.santagraph

import akka.actor._
import models.Models.UserId


class SantaGraph extends Actor with ActorLogging {
  import context._
  import controllers.santagraph.Node._
  import controllers.santagraph.SantaGraph._

  var originalSender: Option[ActorRef] = None

  override def receive: Receive = {
    case FindSolution(graph, goal) =>
      require {
        graph.keySet.contains(goal)
      }
      originalSender = Some(sender())

      graph.keys foreach {
        member => actorOf(Node.props(graph, goal), name = s"$member")
      }

      val start = actorSelection(s"$goal")
      start ! Explore(List(goal))

    case Solution(solution) =>
      originalSender match {
        case Some(source) =>
          source ! solution
          stop(self)

        case None =>
      }
  }

}

object SantaGraph {
  type Graph = Map[UserId, Set[UserId]]

  case class FindSolution(graph: Graph, goal: UserId)
  case class Solution(path: List[UserId])
}
