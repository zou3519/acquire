package acquire.state

import scala.collection.mutable

class TilesManager private(private val _tilesQueue: mutable.Queue[Location],
                           private val _availableTiles: mutable.HashSet[Location],
                           private val _tileRack: IndexedSeq[mutable.HashSet[Location]]) {
  def this(numPlayers: Int) {
    this(_tilesQueue = Locations.newTilesQueue,
      _availableTiles = Locations.newTilesQueue.to[mutable.HashSet],
      _tileRack = Vector.fill(numPlayers)(mutable.HashSet()))
  }

  def tileRack(player: Int) = _tileRack(player)

  def useTile(tile: Location, player: Int): Unit = {
    require(_availableTiles.contains(tile), f"tile $tile%s has not been used")
    require(_tileRack(player).contains(tile), f"player $player%d does not own tile $tile%s")
    _availableTiles -= tile
    _tileRack(player) -= tile
  }

  def drawTile(player: Int): Unit =
    _tileRack(player) += _tilesQueue.dequeue

  def drawUntilFull(player: Int): Unit =
    while (_tileRack(player).size < 6 && _tilesQueue.nonEmpty)
      drawTile(player)

  def copy: TilesManager =
    new TilesManager(_tilesQueue.clone, _availableTiles.clone, _tileRack.map(_.clone))
}
