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

  val aiChoices = Vector("AI lv0: Office Plant", "AI lv1: Intern", "AI lv2: Trader",
    "AI lv3: Manager", "AI lv4: Partner", "AI lv5: CEO", "AI lv∞: Inside Trader")
  val choices = Vector("Human") ++ aiChoices

  val choiceButtons =
    (0 to 4).map {
      case 0 => new SingleChoice(
        choices, 200, 40, Color.web("0892D0"), Color.web("444444"))
      case _ => new SingleChoice(
        aiChoices, 200, 40, Color.web("0892D0"), Color.web("444444"))
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
      case c if c == choices(0) => PlayerType.Human
      case c if c == aiChoices(0) => PlayerType.OfficePlantAi
      case c if c == aiChoices(1) => PlayerType.InternAi
      case c if c == aiChoices(2) => PlayerType.TraderAi
      case c if c == aiChoices(3) => PlayerType.ManagerAi
      case c if c == aiChoices(4) => PlayerType.PartnerAi
      case c if c == aiChoices(5) => PlayerType.CeoAi
      case c if c == aiChoices(6) => PlayerType.InsideTraderAi
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
