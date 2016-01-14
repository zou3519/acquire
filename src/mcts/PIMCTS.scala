package mcts

import acquire.state.{EndTurn, MoveType, Move, AcquireState}

object PIMCTS {
  /**
    * Simulate with respect to a certain player's perspective
    */
  def simulate(startState: AcquireState, player: Int): IndexedSeq[Double] = {
    val state: AcquireState = startState.copy // ForPlayer(player)
    while (!state.isOver) {
      val move: Option[Move] = state.randomMove
      if (move.get.isInstanceOf[EndTurn] && state.canEndGame) {
        state.moveInPlace(EndTurn(move.get.playerId, endGame = true))
      } else {
        state.moveInPlace(move.get)
      }
    }
    state.outcome.get.map(_/10000)
  }

  def UCTSearch[Move](rootNode: PITreeNode[Move], iterMax: Int, timeMax: Int): PITreeNode[Move] = {
    var iter: Int = 0
    val start = System.currentTimeMillis

    while (iter < iterMax && System.currentTimeMillis - start < timeMax) {
      var node = rootNode
//      println("iter: " + iter)

      /* select */
      while (!node.isLeaf && !node.isTerminal) {
        node = node.uctSelectChild.get
      }

      /* expand */
      if (!node.isTerminal) {
        node = node.expand()
      }

      /* simulate */
      val result: IndexedSeq[Double] = simulate(node.state.asInstanceOf[AcquireState], rootNode.state.currentPlayer)

      /* propagate */
      while (node.parent != null) {
        node.update(result)
        node = node.parent
      }
      node.update(result)

      iter += 1
    }
    println("PIUCT search resulted in a move after %d iter and %d ms".format(iter, System.currentTimeMillis() - start))
    //println(rootNode.selfAndKids(0))
    rootNode.children.get.sortBy(_.visits).last
  }
}
