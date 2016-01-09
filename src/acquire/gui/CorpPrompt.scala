package acquire.gui

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment

import acquire.engine.Engine

/**
  * A CorpPrompt is a prompt asking a user to select some number
  * of corporations.
  */
class CorpPrompt(engine: Engine, val message: String) extends Actor {
  _height = 284
  _width = 388

  val button: Button = new Button(50, 50, Color.web("0265ae"), Color.web("aaaaaa"), "OK")

  val corpButtons: IndexedSeq[Button] =
    (0 until 7).map(corp => new CorpButton(engine, corp))

  val checkoutCorpButtons: IndexedSeq[Button] = Vector(new CorpButton(engine, 0), new CorpButton(engine, 4))

  private def corpButtonPosition(num: Int) = (_x + 23 + 50*num, _y + 80)
  private def checkoutCorpButtonPosition(num: Int) = (_x + 23 + 50*num, _y + 150)

  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x, y)
    button.setPosition(_x+330, _y+(284-58))
    corpButtons.indices.foreach(i => {
      val pos = corpButtonPosition(i)
      corpButtons(i).setPosition(pos._1, pos._2)
    })
    checkoutCorpButtons.indices.foreach(i => {
      val pos = checkoutCorpButtonPosition(i)
      checkoutCorpButtons(i).setPosition(pos._1, pos._2)
    })
  }

  override def draw(gc: GraphicsContext): Unit = {
    // background fill
    gc.setFill(Color.web("3a3a3a"))
    gc.fillRect(_x, _y, _width, _height)

    // checkout box fill
    gc.setFill(Color.web("404040"))
    gc.fillRect(_x + 10, _y + 140, _width - 20, 60)

    // outline for prompt and checkout box
    gc.setStroke(Color.web("0093ff"))
    gc.setLineWidth(1)
    gc.strokeRect(_x, _y, _width, _height)
    gc.strokeRect(_x + 10, _y + 140, _width - 20, 60)

    // text message
    gc.setFill(Color.web("aaaaaa"))
    gc.setTextAlign(TextAlignment.CENTER)
    gc.setTextBaseline(VPos.CENTER)
    gc.fillText(message, _x + _width/2, _y + 20)

    button.draw(gc)
    corpButtons.foreach(_.draw(gc))
    checkoutCorpButtons.foreach(_.draw(gc))
  }
}
