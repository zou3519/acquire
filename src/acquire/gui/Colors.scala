package acquire.gui

import javafx.scene.paint.Color

import acquire.state.{CorpTile, OrphanTile, EmptyTile, Tile}

object Colors {
  val colors = Vector("888888", "888888", "f26c8f", "91ea95",
    "f8f691", "5ff0b7", "f2c88c", "5df3f4", "ababeb").map(Color.web)

  def colorOf(tile: Tile) = tile match {
    case EmptyTile() => colors(0)
    case OrphanTile() => colors(1)
    case CorpTile(id) => colors(id + 2)
  }
}