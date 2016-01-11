package acquire.state.impl

import acquire.state._

import scala.collection.mutable
import scala.util.Random

/** Representation of a board with tiles. */
class BoardImpl private(data: Array[Array[Tile]], private var _tiles: mutable.Queue[Location]) extends Board {
  val Rows: Int = Locations.Rows
  val Cols: Int = Locations.Cols

  def this() = this(Array.fill(Locations.Rows,Locations.Cols)(new EmptyTile()), Locations.newTilesQueue)

  override def tileAt(row: Int, col: Int): Tile = data(row)(col)
  override def setTileAt(row: Int, col: Int)(tile: Tile): Unit = data(row)(col) = tile
  override def tileAt(location: Location): Tile = data(location.row)(location.col)
  override def setTileAt(location: Location)(tile: Tile): Unit = data(location.row)(location.col) = tile

  override def tiles = _tiles
  override def shuffleTiles(): Unit = _tiles = Random.shuffle(_tiles)
  override def copy = new BoardImpl(data.map(_.clone), _tiles.clone())
}
