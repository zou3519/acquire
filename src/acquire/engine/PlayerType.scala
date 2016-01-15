package acquire.engine

object PlayerType extends Enumeration {
  type PlayerType = Value
  val Human, CoffeeInternAi, TradingInternAi, TraderAi, InsideTraderAi = Value

  def toAi(playerType: PlayerType): AiPlayer = {
    require(playerType != PlayerType.Human, "cannot instantiate human player")
    playerType match {
      case CoffeeInternAi => new TrivialAi
      case TradingInternAi => new ISMctsAi(4000, 2000)
      case TraderAi => new ISMctsAi(10000, 5000)
      case InsideTraderAi => new PIMctsAi(10000, 5000)
    }
  }
}