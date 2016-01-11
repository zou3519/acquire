package acquire.gui.acquireUI

import acquire.engine.Engine
import acquire.gui.theatre.ActorWithComponents

class Board(val engine: Engine) extends ActorWithComponents {
  val tiles: IndexedSeq[IndexedSeq[TilePiece]] =
    for (row <- 0 until 9) yield
      for (col <- 0 until 12) yield
        new TilePiece(engine, row, col)

  for (row <- 0 until 9; col <- 0 until 12)
    addComponent(tiles(row)(col), x+50*col, y+50*row)
}
