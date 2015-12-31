package acquire.state.impl

import acquire._
import acquire.state._

import scala.collection.mutable
import scala.util.Random

/** Representation of a board with tiles. */
class BoardImpl private(data: Array[Array[Tile]]) extends Board {
  val Rows: Int = Locations.Rows
  val Cols: Int = Locations.Cols

  def this() = this(Array.fill(Locations.Rows,Locations.Cols)(new EmptyTile()))

  override val tiles: mutable.Queue[Location] = new mutable.Queue()
  Random.shuffle(for {
    row <- 0 until Locations.Rows
    col <- 0 until Locations.Cols
  } yield Locations.Store(row)(col)).foreach(tiles.enqueue(_))

  override def tileAt(row: Int, col: Int): Tile = data(row)(col)
  override def setTileAt(row: Int, col: Int)(tile: Tile): Unit = data(row)(col) = tile
  override def tileAt(location: Location): Tile = data(location.row)(location.col)
  override def setTileAt(location: Location)(tile: Tile): Unit = data(location.row)(location.col) = tile

  override def copy = new BoardImpl(data.map(_.clone))
}
