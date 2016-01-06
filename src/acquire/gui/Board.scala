package acquire.gui

import javafx.scene.canvas.GraphicsContext

import acquire.state.AcquireState

class Board(val state: AcquireState) extends Actor {
  val tiles: IndexedSeq[IndexedSeq[TilePiece]] = for (row <- 0 until 9)
    yield for (col <- 0 until 12) yield new TilePiece(state, row, col)

  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x, y)
    for (row <- 0 until 9; col <- 0 until 12) tiles(row)(col).setPosition(x+50*col, y+50*row)
  }

  setPosition(0, 0)

  override def update(): Unit = {
    for (row <- 0 until 9; col <- 0 until 12) tiles(row)(col).update()
  }

  override def draw(gc: GraphicsContext): Unit = {
    for (row <- 0 until 9; col <- 0 until 12) tiles(row)(col).draw(gc)
  }
}
