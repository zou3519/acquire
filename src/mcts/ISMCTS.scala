package mcts

/**
  * Information set MCTS search
  */
object ISMCTS {
  def UCTSearch[Move](rootNode: ISTreeNode[Move], partialState: PartialState[Move], iterMax: Int, timeMax: Int): ISTreeNode[Move] = {
    var iter: Int = 0
    val start = System.currentTimeMillis
    var determinedState = partialState.determinize

    while (iter < iterMax && System.currentTimeMillis - start < timeMax) {
      determinedState = partialState.determinize
      var node = rootNode

      /* select */
      var selectedNodeOpt = node.uctSelectChild(determinedState)
      while (selectedNodeOpt.isDefined) {
        node = selectedNodeOpt.get
        determinedState.moveInPlace(node.move)
        selectedNodeOpt = selectedNodeOpt.get.uctSelectChild(determinedState)
      }

      /* expand */
      val expandedNode = node.expand(determinedState)
      if (expandedNode.isDefined) {
        node = expandedNode.get
        determinedState.moveInPlace(node.move)
      }

      /* simulate */
      val result = determinedState.simulate

      /* propagate */
      while (node.parent != null) {
        node.update(result)
        node = node.parent
      }
      node.update(result)

      iter += 1
    }
    println("ISUCT search resulted in a move after %d iter and %d ms".format(iter, System.currentTimeMillis() - start))
    rootNode.children.values.toIndexedSeq.maxBy(_.visits)
  }
}
