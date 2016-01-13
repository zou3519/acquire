package acquire.engine

import acquire.state.{AcquireState, Move}
import mcts.{UCT, TreeNode}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Abstraction for an AI player.
  * One can ask the AI for a Future Move.
  */
trait AiPlayer {
  def getMove(state: AcquireState): Future[Move]
}

object TrivialAi extends AiPlayer {
  override def getMove(state: AcquireState): Future[Move] = Future {
    state.randomMove.get
  }
}

object ImpossibleAi extends AiPlayer {
  override def getMove(state: AcquireState): Future[Move] = Future {
    val currentNode = new TreeNode[Move](null, state, null)
    currentNode.legalMoves.length match {
      case 1 =>
        currentNode.legalMoves.head
      case _ =>
        val bestChild: TreeNode[Move] = UCT.UCTSearch(currentNode, 10000, 5000)
        bestChild.move
    }
  }
}
