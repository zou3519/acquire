package mcts

/**
  * Abstraction of a game state.
  * @tparam Move The type of a move
  */
trait State[Move] {
  /* player methods. A player is a number in [0, numPlayers) */
  def currentPlayer: Int
  def numPlayers: Int

  /* next state does not mutate but moveInPlace does */
  def copy: State[Move]
  def nextState(move: Move): State[Move]
  def moveInPlace(move: Move): Unit
  def legalMoves: IndexedSeq[Move]
  def randomMove: Option[Move]

  /* after the game has ended, outcome is a vector of the player's scores */
  def outcome: Option[IndexedSeq[Double]]
  def isOver: Boolean

  /* get random results from this state */
  def simulate: IndexedSeq[Double]
}
