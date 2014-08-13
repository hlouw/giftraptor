package controllers.services

import akka.actor.{ActorLogging, Props, Actor}
import models.Models.UserId

class SantaPath(graph: Map[UserId, Set[UserId]], goal: UserId) extends Actor with ActorLogging {
  import controllers.services.SantaPath._

  var doneCount = 0

  override def receive: Receive = {
    case Explore(path) if isSolution(path) =>
      context.parent ! Solution(path)
      context.stop(self)

    case Explore(path) if shortCycle(path) =>
      context.parent ! NoPath
      context.stop(self)

    case Explore(path) =>
      val node = path.head
      for (next <- graph(node)) {
        val nextPath = next :: path
        val actor = context.actorOf(SantaPath.props(graph, goal))
        actor ! Explore(nextPath)
      }

    case Solution(path) => resultReceived {
      context.parent ! Solution(path)
    }

    case NoPath => resultReceived {
      context.parent ! NoPath
    }
  }

  private def resultReceived(f: => Unit) = {
    f
    doneCount += 1
    if (doneCount == context.children.size) context.stop(self)
  }

  private def isSolution(path: List[UserId]): Boolean =
    path.nonEmpty && (path.head == goal) && path.tail.contains(graph.keySet)

  private def shortCycle(path: List[UserId]): Boolean =
    (path.size > 1) && (path.head == goal) && (path.tail.size < graph.size)

}

object SantaPath {

  case class Explore(path: List[UserId])
  case class Solution(path: List[UserId])
  case object NoPath

  def props(graph: Map[UserId, Set[UserId]], goal: UserId) = Props(new SantaPath(graph, goal))
}
