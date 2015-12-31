package acquire.state

sealed trait Tile
case class EmptyTile() extends Tile
case class OrphanTile() extends Tile
case class CorpTile(corpId: Int) extends Tile
