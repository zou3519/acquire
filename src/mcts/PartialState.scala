package mcts

trait PartialState[Move] {
  def determinize: State[Move]
}
