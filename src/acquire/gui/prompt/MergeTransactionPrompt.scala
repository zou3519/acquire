package acquire.gui.prompt

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment

import acquire.engine.{EngineDefaults, Engine}
import acquire.gui.{Default, Button}
import acquire.gui.theatre.{Actor, World}

class MergeTransactionPrompt(engine: Engine) extends Actor {
  _height = 284
  _width = 388

  val message = "Currently merging " + EngineDefaults.corpName(engine.state.preyCorp.get) +
    " into " + EngineDefaults.corpName(engine.state.predatorCorp.get)
//  + "\n"
//    "Player " + engine.state.currentPlayer + ", choose what to do with your shares of " +
//      EngineDefaults.corpName(engine.state.preyCorp.get)

  val submitButton: Button = new Button(50, 50, Color.web("0093ff"), Color.web("0093ff").darker().darker(), "OK")
  val sell: NumberInput = new NumberInput()
  val trade: NumberInput = new NumberInput()
  var keep: Int = engine.state.sheet.shares(engine.state.preyCorp.get, engine.state.currentPlayer)

  sell.up.registerClickHandler((Unit) => {
    if (keep >= 1 && engine.state.sheet.shares(engine.state.preyCorp.get, engine.state.currentPlayer) > sell.value) {
      keep -= 1
      sell.value += 1
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

  def getAmounts: (Int, Int, Int) = (keep, sell.value, trade.value)

  override def addedToWorld(world: World): Unit = {
    world.addActor(sell, x + 60 + 100, y + 150)
    world.addActor(trade, x+60 + 200, y + 150)
    world.addActor(submitButton, x+330, y+(284-58))
  }

  override def removedFromWorld(world: World): Unit = {
    world.removeActor(sell)
    world.removeActor(trade)
    world.removeActor(submitButton)
  }

  override def setPosition(x: Double, y: Double): Unit = {
    if (worldOpt.isDefined) {
      sell.setPosition(x+60 + 100, y+150)
      trade.setPosition(x+60 + 200, y+150)
      submitButton.setPosition(x+330, y+(284-58))
    }
  }

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
    gc.fillText(message, x + _width/2, y + 20)

    gc.fillText("keep", x+60, y + 130)
    gc.fillText("sell", x+160 + 45/2, y + 130)
    gc.fillText("trade 2:1", x+260 + 45/2, y + 130)
    gc.fillText(keep.toString, x + 60, y + 150 + 20)

    submitButton.draw(gc)
  }
}
