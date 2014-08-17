package controllers.santa

import models.Models.UserId
import play.api.Logger
import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Find a Secret Santa path using the power of recursion.
 */
class SantaSolver(graph: Map[UserId, Set[UserId]]) {

  /**
   * Recursively explore all paths in the graph.
   *
   * @param path The path explored thus far.
   * @param goal The final goal to reach.
   * @return A set of solutions.
   */
  private def explore(path: List[UserId], goal: UserId): Set[List[UserId]] = {
    if (isSolution(path, goal))
      Set(path)
    else if (isCycle(path))
      Set(List[UserId]())
    else
      graph(path.head) flatMap { link =>
        explore(link :: path, goal)
      }
  }

  private def isSolution(path: List[UserId], goal: UserId): Boolean =
    path.nonEmpty && (path.head == goal) && graph.keys.forall(path.tail.contains(_))

  private def isCycle(path: List[UserId]): Boolean =
    (path.size > 1) && (path.distinct.size < path.size)

  def solve(goal: UserId) = Future {
    explore(List(goal), goal)
  }

}
