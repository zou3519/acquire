package acquire.state

sealed trait Move {
  val player: Int
}

case class EndTurn(player: Int, endGame: Boolean) extends Move
case class PlaceTile(player: Int, loc: Location) extends Move
case class FoundCorp(player: Int, corp: Int) extends Move
case class MergeCorp(player: Int, preyCorp: Int, predatorCorp: Int) extends Move
case class MergeTransaction(player: Int, preyCorp: Int, predatorCorp: Int, sell: Int, trade: Int) extends Move
case class BuyShares(player: Int, corpToNum: Map[Int, Int]) extends Move

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

