package acquire.state

object AcquireSimulator {
  def simulate(startState: AcquireState): IndexedSeq[Double] = {
    val state: AcquireState = startState.copy
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
}
