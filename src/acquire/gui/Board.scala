package acquire.gui

import javafx.scene.canvas.GraphicsContext

import acquire.engine.Engine
import acquire.gui.theatre.{World, Actor}

class Board(val engine: Engine) extends Actor {
  val tiles: IndexedSeq[IndexedSeq[TilePiece]] = for (row <- 0 until 9)
    yield for (col <- 0 until 12) yield new TilePiece(engine, row, col)

  override def addedToWorld(world: World): Unit = {
    for (row <- 0 until 9; col <- 0 until 12) {
        world.addActor(tiles(row)(col), x+50*col, y+50*row)
    }
  }

  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x, y)
    for (row <- 0 until 9; col <- 0 until 12) {
      if (worldOpt.isDefined)
        tiles(row)(col).setPosition(x+50*col, y+50*row)
    }
  }

  override def update(): Unit = {
    for (row <- 0 until 9; col <- 0 until 12) tiles(row)(col).update()
  }

  override def draw(gc: GraphicsContext): Unit = {
    for (row <- 0 until 9; col <- 0 until 12) tiles(row)(col).draw(gc)
  }
}
