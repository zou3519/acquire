package acquire.engine

import acquire.state.{AcquireState, Move}
import mcts.{ISMCTS, ISTreeNode, PIMCTS, PITreeNode}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Abstraction for an AI player.
  * One can ask the AI for a Future Move.
  */
trait AiPlayer {
  def getMove(state: AcquireState): Future[Move]
}

class TrivialAi extends AiPlayer {
  override def getMove(state: AcquireState): Future[Move] = Future {
    state.randomMove.get
  }
}

class PIMctsAi(maxIter: Int, maxMillis: Int) extends AiPlayer {
  def this() {
    this(10000, 5000)
  }

  override def getMove(state: AcquireState): Future[Move] = Future {
    val currentNode = new PITreeNode[Move](null, state, null)
    currentNode.legalMoves.length match {
      case 1 =>
        currentNode.legalMoves.head
      case _ =>
        val bestChild: PITreeNode[Move] = PIMCTS.UCTSearch(currentNode, maxIter, maxMillis)
        bestChild.move
    }
  }
}

class ISMctsAi(maxIter: Int, maxMillis: Int) extends AiPlayer {
  def this() {
    this(10000, 5000)
  }

  override def getMove(state: AcquireState): Future[Move] = Future {
    val currentNode = new ISTreeNode[Move](null, null, state.numPlayers)
    val legalMoves = state.legalMoves
    legalMoves.length match {
      case 1 =>
        legalMoves.head
      case _ =>
        val bestChild: ISTreeNode[Move] = ISMCTS.UCTSearch(currentNode, state, maxIter, maxMillis)
        bestChild.move
    }
  }
}
