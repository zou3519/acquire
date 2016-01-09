package acquire.engine

import acquire.engine.PlayerType.PlayerType
import acquire.state._

object EngineDefaults {
  val Corps = Seq("Tower", "Luxor", "American", "Worldwide", "Festival", "Imperial", "Continental").zip(
    Seq(200, 200, 300, 300, 300, 400, 400))

  def moveToString(move: Move) = move match {
    case EndTurn(player, endGame) =>
      val nextPlayer = (player + 1)%4
      if (endGame) f"player $player%d ended the game." else f"--------------- player $nextPlayer%d's turn ---------------"
    case PlaceTile(player, loc) => f"player $player%d placed tile $loc%s"
    case FoundCorp(player, corp) => f"player $player%d founded corp $corp%d"
    case MergeCorp(player, preyCorp, predatorCorp) => f"player $player%d merged corp $preyCorp%d into corp $predatorCorp%d"
    case MergeTransaction(player, preyCorp, predatorCorp, sell, trade) => f"player $player%d sold $sell%d and traded $trade%d shares of $preyCorp%d"
    case BuyShares(player, corpToNum) =>
      if (corpToNum.isEmpty) f"player $player%d bought no shares"
      else f"player $player%d bought " + corpToNum.map {
        case (corp, amt) => amt.toString + " shares of corp " + corp.toString
      }.mkString(" and ")
  }
}

class Engine(players: IndexedSeq[(String, PlayerType)]) {
  private var _state: AcquireState = new AcquireState(new Config(players.map(_._1), EngineDefaults.Corps))

  private var _numMoves: Int = 0                              // # moves people have made
  private var _history: IndexedSeq[TurnRecord] = Vector()     // list of (state, move chosen from this state)
  private var _currentTurnRecord: TurnRecord = new TurnRecord // the current turn record
  private var _corpHQ: Map[Int, Option[Location]] =           // map of corp to corp headquarters
    (0 until 7).map(i => (i, None)) toMap

  def state: AcquireState = _state
  def history: IndexedSeq[TurnRecord] = _history
  def corpHQ: Map[Int, Option[Location]] = _corpHQ
  def currentTurnRecord: TurnRecord = _currentTurnRecord
  def numMoves: Int = _numMoves
  def flatHistory: IndexedSeq[MoveRecord] = _history.flatMap(_.records) ++ _currentTurnRecord.records
  def currentPlayerType = players(state.currentPlayer)._2

  def makeMove(move: Move): Unit = {
    // TODO: check to see if the move is legal ?
    val nextState = _state.nextState(move)

    // update history
    _currentTurnRecord = _currentTurnRecord.addRecord(new MoveRecord(_state, move))
    if (_state.expectedMoveType == MoveType.EndTurnT) {
      _history = _history :+ _currentTurnRecord
      _currentTurnRecord = new TurnRecord
    }

    // keep track of corpHQ metadata
    move match {
      case FoundCorp(_,corp) => _corpHQ = _corpHQ.updated(corp, Some(_state.tilePlaced.get))
      case MergeCorp(_,prey,_) => _corpHQ = _corpHQ.updated(prey, None)
      case _ => ()
    }

    // prepare for the next move
    _state = nextState
    _numMoves += 1
  }
}
