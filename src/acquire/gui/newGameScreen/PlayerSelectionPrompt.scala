package acquire.gui.newGameScreen

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.{FontWeight, Font, TextAlignment}

import acquire.engine.PlayerType.PlayerType
import acquire.engine.{PlayerType, Engine}
import acquire.engine.PlayerType.PlayerType
import acquire.gui.acquireUI.AcquireWorld
import acquire.state.Config
import theatre.core.{Theatre, ActorWithComponents}
import theatre.ui.{Button, SingleChoice}

class PlayerSelectionPrompt extends ActorWithComponents {
  val choiceButtons =
    (0 to 4).map {
      case 0 => new SingleChoice(
        Vector("Human"), 200, 40, Color.web("0892D0"), Color.web("444444"))
      case _ => new SingleChoice(
        Vector("AI-Trivial", "AI-Impossible"), 200, 40, Color.web("0892D0"), Color.web("444444"))
    }
  private val font: Font = Font.font("Helvetica", FontWeight.BOLD, 16)
  private val headingFont: Font = Font.font("Helvetica", FontWeight.BOLD, 40)

  val submitButton: Button = new Button(60, 40, Color.web("74C365"), Color.web("74C365").darker().darker(), "Start")

  init()

  private def init() = {
    for (i <- 0 until 4) {
      addComponent(choiceButtons(i), 240, 100 + 20 + 60*i)
    }
    addComponent(submitButton, 500, 300)
    submitButton.registerClickHandler((Unit) => {
      val config: IndexedSeq[(String, PlayerType)] =
        (0 until 4).map(i => ("p" + i, sToPlayerType(choiceButtons(i).getChoice)))
      val engine: Engine = new Engine(config)
      val newWorld: AcquireWorld = new AcquireWorld(engine)
      val theatre: Theatre = worldOpt.get.theatre.get
      theatre.setWorld(newWorld)
    })
  }

  private def sToPlayerType(s: String): PlayerType = {
    s match {
      case "Human" => PlayerType.Human
      case "AI-Trivial" => PlayerType.TrivialAi
      case "AI-Impossible" => PlayerType.ImpossibleAi
    }
  }

  override def draw(gc: GraphicsContext): Unit = {
    super.draw(gc)

    gc.setFill(Color.web("707070"))
    gc.setFont(font)
    gc.setTextAlign(TextAlignment.CENTER)
    gc.setTextBaseline(VPos.CENTER)
    for (i <- 0 until 4) {
      gc.fillText(f"player $i%d:", x+ 170, 100 + y + 20 + 60*i + 20)
    }
    gc.setFont(headingFont)
    gc.fillText("New Game", 600/2, 50)
  }
}
