package acquire.state

import scala.collection.mutable
import scala.util.Random

class Location private[state](val row: Int, val col: Int, nRows: Int, nCols: Int) {
  private val delta = List((0,1), (0, -1), (-1, 0), (1, 0))

  /**
    * neighbors is lazy evaluated because it has to be evaluated after the static Locations object
    * has been instantiated.
    */
  lazy val neighbors: List[Location] = for {
    (dr, dc) <- delta
    if inRange(dr+row, 0, nRows) && inRange(dc+col, 0, nCols)
  } yield Locations.Store(row+dr)(col+dc)

  override def toString: String = (col + 1).toString + numToChar(row).get.toString

  private def inRange(num: Int, lower: Int, upper: Int) = lower <= num && num < upper

  private def numToChar(num: Int): Option[Char] =
    if (num >= 0 && num < 26) Some((num+65).toChar) else None
}

/**
  * Locations is a static store of all board locations.
  * Call Locations.Store(row, col) to get a specific tile.
  */
object Locations {
  val Rows = 9
  val Cols = 12
  val Store: IndexedSeq[IndexedSeq[Location]] =
    (0 until Rows).map(row => (0 until Cols).map(col => new Location(row, col, Rows, Cols)))

  /**
    * Get a location via string representation. No error checking.
    * @param loc A string representation (eg, "1A")
    * @return The location corresponding to the string representation.
    */
  def get(loc: String): Location =
    loc.splitAt(loc.length()-1) match {
      case (col, row) => Store(row.charAt(0) - 65)(col.toInt - 1)
    }

  /**
    * Creates a queue of all tiles in the store
    * @return A mutable.Queue of Locations
    */
  def newTilesQueue: mutable.Queue[Location]  =
    new mutable.Queue[Location] ++= Random.shuffle(for {
      row <- 0 until Locations.Rows
      col <- 0 until Locations.Cols
    } yield Locations.Store(row)(col))
}
