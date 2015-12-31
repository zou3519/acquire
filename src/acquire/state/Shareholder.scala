package acquire.state

object Shareholder extends Enumeration {
  type Shareholder = Value
  val Majority, Minority, Regular = Value
}
