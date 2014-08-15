package controllers.santa

import models.Models.UserId

/**
 * Find a Secret Santa path using the power of recursion.
 */
class SantaPath(graph: Map[UserId, Set[UserId]]) {

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

    def isSolution(path: List[UserId], goal: UserId): Boolean =
      path.nonEmpty && (path.head == goal) && graph.keys.forall(path.tail.contains(_))

    def isCycle(path: List[UserId]): Boolean =
      (path.size > 1) && (path.distinct.size < path.size)

  }

  def solve(goal: UserId) = explore(List(goal), goal)

}
