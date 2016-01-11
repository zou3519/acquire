package acquire.gui.acquireUI

import javafx.scene.paint.Color

import acquire.engine.Engine
import acquire.gui.Default
import acquire.gui.theatre.World

class AcquireWorld(engine: Engine) extends World(1024, 768) {

  override def buildWorld(): Unit = {
    val board: Board = new Board(engine)
    val sheet: ScoreSheet = new ScoreSheet(engine)
    val history: History = new History(engine)
    val game: AcquireGame = new AcquireGame(engine, board, sheet)

    this.addActor(board, 10, 10)
    this.addActor(sheet, 684, 34)
    this.addActor(history, 10, 470)
    this.addActor(game, 0, 0)
  }

  override def drawWorldBackground(): Unit = {
    gc.clearRect(0, 0, 1024, 768)
    gc.setFill(new Color(0.2, 0.2, 0.2, 1))
    gc.fillRect(0, 0, canvas.getWidth, canvas.getHeight)
    gc.setFill(Color.web("707070"))
    gc.fillText(Default.VersionString, 650, 20)
  }
}
