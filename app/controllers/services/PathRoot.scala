package controllers.services

import akka.actor.{ActorLogging, ActorRef, Actor}
import controllers.services.SantaPath.{Solution, Explore}
import models.Models.UserId

class PathRoot extends Actor with ActorLogging {
  import controllers.services.PathRoot._

  var originalSender: Option[ActorRef] = None

  override def receive: Receive = {
    case FindSolution(graph, goal) =>
      originalSender = Some(sender())
      val first = context.actorOf(SantaPath.props(graph, goal), s"Root$goal")
      first ! Explore(List(goal))

    case Solution(path) =>
      originalSender.map(actor => actor ! Shutdown)
      context.become(shuttingDown)
      self ! Shutdown
  }

  def shuttingDown: Receive = {
    case Shutdown =>
      context.stop(self)

    case _ =>
  }
}

object PathRoot {
  case class FindSolution(graph: Map[UserId, Set[UserId]], goal: UserId)
  case object Shutdown
}
