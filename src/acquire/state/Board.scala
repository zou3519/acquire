package acquire.state

import scala.collection.mutable

/**
  * The Board is the grid portion of the Acquire game
  */
trait Board {
  def Rows: Int
  def Cols: Int

  /* queue of tiles that have not been handed to the players yet */
  def tiles: mutable.Queue[Location]

  def tileAt(row: Int, col: Int): Tile
  def setTileAt(row: Int, col: Int)(tile: Tile): Unit
  def tileAt(loc: Location): Tile
  def setTileAt(loc: Location)(tile: Tile): Unit

  /* make a deep copy */
  def copy: Board
}
