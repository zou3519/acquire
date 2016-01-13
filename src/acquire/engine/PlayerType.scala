package acquire.engine

object PlayerType extends Enumeration {
  type PlayerType = Value
  val Human, TrivialAi, ImpossibleAi = Value
}