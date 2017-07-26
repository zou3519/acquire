package mcts

import scala.collection.mutable
import scala.util.Random

class ISTreeNode[Move](val parent: ISTreeNode[Move], val move: Move, numPlayers: Int) {
  private val _children: mutable.Map[Move, ISTreeNode[Move]] = mutable.Map()

  /* ISTreeNode holds the number of visits and a stats array with entries corresponding to players */
  val stats: Array[Double] = Array.fill(numPlayers)(0)
  var visits: Int = 0

  /* the UCT1 function, from the viewpoint of the player who is about to move */
  def uct1(state: MCTSStateLike[Move]): (ISTreeNode[Move] => Double) = node => {
    if (visits == 0 || node.visits == 0) Double.PositiveInfinity
    else node.stats(state.currentPlayer) / node.visits + math.sqrt(2 * math.log(visits) / node.visits)
  }

  def children: mutable.Map[Move, ISTreeNode[Move]] = _children

  /* Select a child based on the UCT criteria */
  def uctSelectChild(state: MCTSStateLike[Move]): Option[ISTreeNode[Move]] = {
    val legalMoves = state.legalMoves

    // If this node doesn't contain any of the state's possible states, return None for no selection
    if (legalMoves.forall(move => !_children.contains(move)))
      return None

    // If there are any moves that the node doesn't have, add them to this node
    updateChildrenWithNewMoves(legalMoves)

    // Perform UCT selection
    Some(children(legalMoves).maxBy(uct1(state)))
  }

  /* expand the ISTreeNode using the state */
  def expand(state: MCTSStateLike[Move]): Option[ISTreeNode[Move]] = {
    val legalMoves = state.legalMoves
    if (legalMoves.isEmpty) None
    else {
      updateChildrenWithNewMoves(legalMoves)
      val randomMove = legalMoves(Random.nextInt(legalMoves.length))
      Some(_children(randomMove))
    }
  }

  /* Update this node with the result from a simulation */
  def update(result: IndexedSeq[Double]): Unit = {
    visits += 1
    for (i <- stats.indices) {
      stats(i) += result(i)
    }
  }

  /* Returns the children of this ISTreeNode that result from a Move in moves */
  private def children(moves: IndexedSeq[Move]): IndexedSeq[ISTreeNode[Move]] =
    moves map _children

  /* If there are any moves that the node doesn't have, add them to this node as children */
  private def updateChildrenWithNewMoves(moves: IndexedSeq[Move]): Unit =
    for (move <- moves)
      if (!_children.contains(move))
        _children.put(move, new ISTreeNode[Move](this, move, numPlayers))

  override def toString: String = {
    move + " -- " + stats.mkString(",") + " visits: " + visits.toString
  }

  /* return a string representation of the tree starting from this node recursively */
  def treeToString(indent: Int): String = {
    var s = padFront(toString, indentString(indent))
    if (children.nonEmpty) s += "\n" + _children.map {
      case (_, node) => node.treeToString(indent+1)
    }.mkString("\n")
    s
  }

  /* return a string representation of this node and only it's children */
  def selfAndKids(indent: Int): String = {
    var s = padFront(toString, indentString(indent))
    if (children.nonEmpty) s += "\n" +  _children.map{
      case (_, c) => padFront(c.toString, indentString(indent+1))
    }.mkString("\n")
    s
  }

  private def indentString(indent: Int): String =
    (for (i <- 1 until indent+1) yield {"  | "}).mkString("")

  private def padFront(str: String, padding: String): String =
    str.split('\n').map(padding + _).mkString("\n")
}
