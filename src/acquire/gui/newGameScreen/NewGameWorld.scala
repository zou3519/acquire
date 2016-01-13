package acquire.gui.newGameScreen

import javafx.scene.paint.Color

import theatre.core.World
import theatre.ui.SingleChoice

class NewGameWorld extends World(600,400) {
  private val choices = Vector("Human", "AI-Trivial", "AI-Impossible")

  override def buildWorld(): Unit = {
    val selectionPrompt = new PlayerSelectionPrompt
    addActor(selectionPrompt, 0, 0)
  }

  override def drawWorldBackground(): Unit = {
    gc.clearRect(0, 0, 600, 400)
    gc.setFill(new Color(0.2, 0.2, 0.2, 1))
    gc.fillRect(0, 0, canvas.getWidth, canvas.getHeight)
  }
}
