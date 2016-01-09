package acquire.engine

import acquire.state.{Move, AcquireState}

sealed trait Record
/**
  * A record of a turn
  * @param records the records of the moves that occurred this turn
  */
case class TurnRecord(records: IndexedSeq[MoveRecord]) extends Record {
  def this() { this(Vector()) }
  def addRecord(record: MoveRecord): TurnRecord =
    new TurnRecord(records :+ record)
}

/**
  * A record of a move decision
  * @param state the state at which the move was made
  * @param move the move someone made at this state
  */
case class MoveRecord(state: AcquireState, move: Move) extends Record {
  val description = EngineDefaults.describeMoveRecord(this)
}
