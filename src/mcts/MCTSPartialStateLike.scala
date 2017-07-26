package mcts

trait MCTSPartialStateLike[Move] {
  def determinize: MCTSStateLike[Move]
}
