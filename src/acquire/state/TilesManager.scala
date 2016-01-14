package acquire.state

import scala.collection.mutable
import scala.util.Random

class TilesManager private(val _tilesQueue: mutable.Queue[Location],
                           val _availableTiles: mutable.HashSet[Location],
                           val _tileRack: IndexedSeq[mutable.HashSet[Location]]) {
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

  // randomizes the tiles EXCEPT for the player
  def randomizeTiles(exceptPlayer: Int): Unit = {
    // maps player id => tiles
    val numTiles: Map[Int, Int] = _tileRack.indices.map(i => (i, _tileRack(i).size)).toMap
    val playersToShuffle: Seq[Int] = _tileRack.indices.filter(_ != exceptPlayer)

    val shuffledTiles = Random.shuffle(_availableTiles.to[mutable.Queue])

    // give players their tiles
    for (player <- playersToShuffle) {
      while (_tileRack(player).nonEmpty)
        _tileRack(player) -= _tileRack(player).head
      while (_tileRack(player).size < numTiles(player)) {
        val tile = shuffledTiles.dequeue()
        if (!_tileRack(exceptPlayer).contains(tile)) {
          _tileRack(player) += tile
        }
      }
    }

    // put the remaining tiles into the tiles queue
    _tilesQueue.dequeueAll(l => true)
    while (shuffledTiles.nonEmpty) {
      val tile = shuffledTiles.dequeue()
      if (!_tileRack(exceptPlayer).contains(tile)) {
        _tilesQueue += tile
      }
    }
  }
}
