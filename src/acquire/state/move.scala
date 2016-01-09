package acquire.state

sealed trait Move {
  val playerId: Int
}

case class EndTurn(playerId: Int, endGame: Boolean) extends Move
case class PlaceTile(playerId: Int, location: Location) extends Move
case class FoundCorp(playerId: Int, corpId: Int) extends Move
case class MergeCorp(playerId: Int, preyCorpId: Int, predatorCorp: Int) extends Move
case class MergeTransaction(playerId: Int, preyCorpId: Int, predatorCorpId: Int, sellAmt: Int, tradeAmt: Int) extends Move
case class BuyShares(playerId: Int, sharesMap: Map[Int, Int]) extends Move

object MoveType extends Enumeration {
  type MoveType = Value
  val EndTurnT, PlaceTileT, FoundCorpT, MergeCorpT, MergeTransactionT, BuySharesT = Value

  def typeOf(move: Move) = move match {
    case EndTurn(_,_) => MoveType.EndTurnT
    case BuyShares(_,_) => MoveType.BuySharesT
    case FoundCorp(_,_) => MoveType.FoundCorpT
    case PlaceTile(_,_) => MoveType.PlaceTileT
    case MergeCorp(_,_,_) => MoveType.MergeCorpT
    case MergeTransaction(_,_,_,_,_) => MoveType.MergeTransactionT
  }
}

