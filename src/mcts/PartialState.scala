package mcts

trait PartialState[Move] {
  def determinize: MCTSStateLike[Move]
}
