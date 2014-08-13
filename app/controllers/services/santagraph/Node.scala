package controllers.services.santagraph

import akka.actor.{Props, Actor, ActorLogging}
import controllers.services.santagraph.SantaGraph.{Solution, Graph}
import models.Models.UserId

class Node(graph: Graph, goal: UserId) extends Actor with ActorLogging {
  import controllers.services.santagraph.Node._
  import context._

  override def receive: Receive = {
    case Explore(path) if isSolution(path) =>
      log.info(s"Found solution: $path")
      parent ! Solution(path)

    case Explore(path) if isCycle(path) =>

    case Explore(path) =>
      graph(path.head) foreach { link =>
        val next = actorSelection(s"../$link")
        next ! Explore(link :: path)
      }
  }

  private def isSolution(path: List[UserId]): Boolean =
    path.nonEmpty && (path.head == goal) && graph.keySet.forall(path.tail.contains(_))

  private def isCycle(path: List[UserId]): Boolean =
    (path.size > 1) && (path.toSet.size < path.size)

}

object Node {
  case class Explore(path: List[UserId])

  def props(graph: Graph, goal: UserId) = Props(new Node(graph, goal))
}
