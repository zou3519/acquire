package mcts

object PIMCTS {

  def UCTSearch[Move](rootNode: PITreeNode[Move], iterMax: Int, timeMax: Int): PITreeNode[Move] = {
    var iter: Int = 0
    val start = System.currentTimeMillis

    while (iter < iterMax && System.currentTimeMillis - start < timeMax) {
      var node = rootNode

      /* select */
      while (!node.isLeaf && !node.isTerminal) {
        node = node.uctSelectChild.get
      }

      /* expand */
      if (!node.isTerminal) {
        node = node.expand()
      }

      /* simulate */
      val result = node.state.simulate

      /* propagate */
      while (node.parent != null) {
        node.update(result)
        node = node.parent
      }
      node.update(result)

      iter += 1
    }
    println("PIUCT search resulted in a move after %d iter and %d ms".format(iter, System.currentTimeMillis() - start))
    rootNode.children.get.maxBy(_.visits)
  }
}
