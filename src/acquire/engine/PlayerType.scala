package acquire.engine

object PlayerType extends Enumeration {
  type PlayerType = Value
  val Human, OfficePlantAi, InternAi, TraderAi, ManagerAi, PartnerAi, InsideTraderAi = Value

  def toAi(playerType: PlayerType): AiPlayer = {
    require(playerType != PlayerType.Human, "cannot instantiate human player")
    playerType match {
      case OfficePlantAi => new TrivialAi
      case InternAi => new ISMctsAi(2000, 1000)
      case TraderAi => new ISMctsAi(4000, 2000)
      case ManagerAi => new ISMctsAi(10000, 5000)
      case PartnerAi => new ISMctsAi(16000, 8000)
      case InsideTraderAi => new PIMctsAi(10000, 5000)
    }
  }
}