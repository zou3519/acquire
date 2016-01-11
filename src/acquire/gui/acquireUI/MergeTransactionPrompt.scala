package acquire.gui.acquireUI

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment

import acquire.engine.Engine
import theatre.core.{ActorWithComponents, Actor, World}
import theatre.ui.{NumberInput, Button}

class MergeTransactionPrompt(engine: Engine) extends ActorWithComponents {

  val submitButton: Button = new Button(50, 50, Color.web("0093ff"), Color.web("0093ff").darker().darker(), "OK")
  val sell: NumberInput = new NumberInput()
  val trade: NumberInput = new NumberInput()
  var keep: Int = engine.state.sheet.shares(engine.state.preyCorp.get, engine.state.currentPlayer)

  val message = {
    val preyCorp = engine.state.preyCorp.get
    val predatorCorp = engine.state.predatorCorp.get
    val preyName: String = engine.config.corpName(preyCorp)
    val predatorName: String = engine.config.corpName(predatorCorp)
    val preyPrice: Int = engine.state.sheet.sharePrice(preyCorp).get
    val predatorPrice: Int = engine.state.sheet.sharePrice(predatorCorp).get

    Vector(
      f"$preyName will be merged into $predatorName",
      f"Please select how many shares of ",
      f"$preyName ($$$preyPrice%d/ea) you would like to keep,",
      f"sell, or trade 2:1 for shares of $predatorName ($$$predatorPrice%d/ea)"
    )
  }

  init()

  private def init(): Unit = {
    _height = 284
    _width = 388

    addComponent(sell, 60+100, 150)
    addComponent(trade, 60+200, 150)
    addComponent(submitButton, 330, 284-58)

    sell.up.registerClickHandler((Unit) => {
      if (keep >= 1 && engine.state.sheet.shares(engine.state.preyCorp.get, engine.state.currentPlayer) > sell.value) {
        keep -= 1; sell.value += 1
      }
    })
    sell.down.registerClickHandler((Unit) => {
      if (sell.value >= 1) {
        keep += 1
        sell.value -= 1
      }
    })
    trade.up.registerClickHandler((Unit) => {
      if (keep >= 2 && engine.state.sheet.bankShares(engine.state.predatorCorp.get) > trade.value/2 ) {
        keep -= 2
        trade.value += 2
      }
    })
    trade.down.registerClickHandler((Unit) => {
      if (trade.value >= 2) {
        keep += 2
        trade.value -= 2
      }
    })
  }

  def getAmounts: (Int, Int, Int) = (keep, sell.value, trade.value)

  override def draw(gc: GraphicsContext): Unit = {
    // background fill
    gc.setFill(Color.web("3a3a3a"))
    gc.fillRect(x, y, _width, _height)

    // checkout box fill
    gc.setFill(Color.web("404040"))
    gc.fillRect(x + 10, y + 140, _width - 20, 60)

    // outline for prompt and checkout box
    gc.setStroke(Color.web("0093ff"))
    gc.setLineWidth(1)
    gc.strokeRect(x, y, _width, _height)
    gc.strokeRect(x + 10, y + 140, _width - 20, 60)

    // text message
    gc.setFill(Color.web("aaaaaa"))
    gc.setTextAlign(TextAlignment.CENTER)
    gc.setTextBaseline(VPos.CENTER)
    for (index <- message.indices) {
      gc.fillText(message(index),  x + _width/2, y + 20 + 20*index)
    }

    gc.fillText("keep", x+60, y + 130)
    gc.fillText("sell", x+160 + 45/2, y + 130)
    gc.fillText("trade 2:1", x+260 + 45/2, y + 130)
    gc.fillText(keep.toString, x + 60, y + 150 + 20)

    super.draw(gc)
  }
}
