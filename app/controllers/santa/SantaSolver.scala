package controllers.santa

import scala.util.Random

import models.SantaModel._
import models.UserModel._
import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Find a Secret Santa path using the power of recursion.
 */
class SantaSolver(graph: Map[UserId, Set[UserId]]) {

  /**
   * Random a valid graph traversal, if it exists. Traversal is randomised, so the same solution
   * won't be provided on each run.
   *
   * @param candidates
   * @param goal
   * @return
   */
  private def explore(candidates: List[Path], goal: UserId): Option[Path] = {
    if (candidates.isEmpty)
      None
    if (isSolution(candidates.head, goal))
      Some(candidates.head)
    else if (isCycle(candidates.head))
      explore(candidates.tail, goal)
    else {
      val candidatePath = candidates.head
      val newCandidates: Set[Path] = graph(candidatePath.head) map { next =>
        next :: candidatePath
      }
      val shuffled = Random.shuffle(newCandidates.toList)
      explore(shuffled ++ candidates.tail, goal)
    }
  }

  private def isSolution(path: Path, goal: UserId): Boolean =
    path.nonEmpty && (path.head == goal) && graph.keys.forall(path.tail.contains(_))

  private def isCycle(path: Path): Boolean =
    (path.size > 1) && (path.distinct.size < path.size)

  /**
   *
   * @param goal
   * @return
   */
  def solve(goal: UserId): Future[Option[Path]] = Future {
    val path = List(goal)
    explore(List(path), goal)
  }

}

object SantaSolver {
  def apply(graph: Map[UserId, Set[UserId]]) = new SantaSolver(graph)
}
