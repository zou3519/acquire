package acquire.engine

object PlayerType extends Enumeration {
  type PlayerType = Value
  val Human, TrivialAi, ISMCTSAi, ImpossibleAi = Value
}