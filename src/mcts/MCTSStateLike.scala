package mcts

/**
  * Abstraction of a game state.
  * @tparam Move The type of a move
  */
trait MCTSStateLike[Move] {
  /* player methods. A player is a number in [0, numPlayers) */
  def currentPlayer: Int
  def numPlayers: Int

  /* Make a copy of the game state */
  def copy: MCTSStateLike[Move]

  /* next state does not mutate but moveInPlace does */
  def nextState(move: Move): MCTSStateLike[Move]
  def moveInPlace(move: Move): Unit

  def legalMoves: IndexedSeq[Move]

  /* get random results from this state */
  def simulate: IndexedSeq[Double]
}
