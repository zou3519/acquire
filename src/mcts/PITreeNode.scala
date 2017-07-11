package mcts

import scala.math
import scala.util.Random

/**
  * Representation of an MCTS game tree node.
  * @tparam Move The type of a move in this game state
  * @param parent The node's parent
  * @param state The node's corresponding state
  * @param move The move that resulted in this node's state from the parent
  */
class PITreeNode[Move](var parent: PITreeNode[Move], val state: MCTSStateLike[Move], val move: Move) {
  val legalMoves = state.legalMoves

  /* TreeNode has children only when it has been expanded */
  var expanded: Boolean = false
  private lazy val _children: IndexedSeq[PITreeNode[Move]] =
    legalMoves map { move => new PITreeNode[Move](this, state.nextState(move), move) }

  /* TreeNode holds the number of visits and a stats array with entries corresponding to players */
  val stats: Array[Double] = Array.fill(state.numPlayers)(0)
  var visits: Int = 0

  /* TreeNode can be a leaf of the current (in view) game tree, or a terminal node of the full tree */
  val isTerminal: Boolean = legalMoves.isEmpty
  def isRoot: Boolean = parent == null
  def isLeaf: Boolean = !expanded

  /* the UCT1 function, from the viewpoint of the player who is about to move */
  val uct1: (PITreeNode[Move] => Double) = node => {
    if (visits == 0 || node.visits == 0) Double.PositiveInfinity
    else node.stats(state.currentPlayer) / node.visits + math.sqrt(2 * math.log(visits) / node.visits)
  }

  /* children only exist if this node has been expanded */
  def children: Option[IndexedSeq[PITreeNode[Move]]] = if (expanded) Some(_children) else None

  /* returns the node that results from applying the move to the current state */
  def nextNode(move: Move): Option[PITreeNode[Move]] = {
    if (!expanded) return None
    val index: Int = legalMoves.indexOf(move)
    if (index >= 0) Some(_children(index)) else None
  }

  /* Not reversable! makes this node the new root of the tree */
  def makeRoot(): Unit = parent = null

  /* select a child based on the UCT criteria */
  def uctSelectChild: Option[PITreeNode[Move]] = if (expanded) Some(_children.sortBy(uct1).last) else None

  /* expand the TreeNode */
  def expand(): PITreeNode[Move] = {
    expanded = true
    _children(Random.nextInt(_children.length))
  }

  /* Update this node with the result from a simulation */
  def update(result: IndexedSeq[Double]): Unit = {
    visits += 1
    for (i <- stats.indices) {
      stats(i) += result(i)
    }
  }

  override def toString: String = {
    val s: String = stats.mkString(",") + " visits: " + visits.toString + "\n" + state.toString
    if (parent == null) s else "uct: " + parent.uct1(this).toString.substring(0, 6) + " " + s
  }

  /* return a string representation of the tree starting from this node recursively */
  def treeToString(indent: Int): String = {
    var s = padFront(toString, indentString(indent))
    if (expanded) s += "\n" + _children.map(_.treeToString(indent+1)).mkString("\n")
    s
  }

  /* return a string representation of this node and only it's children */
  def selfAndKids(indent: Int): String = {
    var s = padFront(toString, indentString(indent))
    if (expanded) s += "\n" +  _children.map(c => padFront(c.toString, indentString(indent+1))).mkString("\n")
    s
  }

  private def indentString(indent: Int): String =
    (for (i <- 1 until indent+1) yield {"  | "}).mkString("")

  private def padFront(str: String, padding: String): String =
    str.split('\n').map(padding + _).mkString("\n")
}
