package acquire.gui

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment

import acquire.engine.Engine

import scala.collection.mutable.ArrayBuffer

/**
  * A CorpPrompt is a prompt asking a user to select some number
  * of corporations.
  */
abstract class CorpPrompt(engine: Engine, val message: String) extends Actor {
  _height = 284
  _width = 388

  val submitButton: Button = new Button(50, 50, Color.web("0093ff"), Color.web("0093ff").darker().darker(), "OK")

  val corpButtons: ArrayBuffer[CorpButton]
  val checkoutCorpButtons: ArrayBuffer[CorpButton] = ArrayBuffer()

  private def corpButtonPosition(num: Int) = (_x + 23 + 50*num, _y + 80)
  private def checkoutCorpButtonPosition(num: Int) = (_x + 23 + 50*num, _y + 150)

  override def addedToWorld(world: World): Unit = {
    world.addActor(submitButton, _x+330, _y+(284-58))
    corpButtons.indices.foreach(i => {
      val pos = corpButtonPosition(i)
      world.addActor(corpButtons(i), pos._1, pos._2)
    })
  }

  override def removedFromWorld(world: World): Unit = {
    corpButtons.foreach(world.removeActor)
    checkoutCorpButtons.foreach(world.removeActor)
    world.removeActor(submitButton)
  }

  protected def addCheckoutCorpButton(corp: Int): Unit = {
    val newButton: CorpButton = new CorpButton(engine, corp)
    checkoutCorpButtons += newButton
    val pos = checkoutCorpButtonPosition(checkoutCorpButtons.length - 1)
    if (worldOpt.isDefined) {
      worldOpt.get.addActor(newButton, pos._1, pos._2)
    }
    newButton.registerClickHandler((Unit) => {
      checkoutCorpButtons -= newButton
      worldOpt.get.removeActor(newButton)
    })
  }

  def selectedCorps: Map[Int, Int] =
    for ((group, list) <- checkoutCorpButtons.map(_.corp).groupBy(i => i)) yield (group, list.length)


  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x,y)
    if (worldOpt.isDefined) {
      submitButton.setPosition(_x + 330, _y + (284 - 58))
      corpButtons.indices.foreach(i => {
        val pos = corpButtonPosition(i)
        corpButtons(i).setPosition(pos._1, pos._2)
      })
      checkoutCorpButtons.indices.foreach(i => {
        val pos = checkoutCorpButtonPosition(i)
        checkoutCorpButtons(i).setPosition(pos._1, pos._2)
      })
    }
  }

  override def update(): Unit = {
    super.update()
    submitButton.update()
    corpButtons.foreach(_.update())

    // only update these folks if there has been NO CHANGE
    val currentLength = checkoutCorpButtons.length
    checkoutCorpButtons.foreach(button => if (currentLength == checkoutCorpButtons.length) button.update())

    if (currentLength != checkoutCorpButtons.length) {
      setPosition(_x, _y) // refresh the position of the buttons.
    }
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

    submitButton.draw(gc)
    corpButtons.foreach(_.draw(gc))
    checkoutCorpButtons.foreach(_.draw(gc))
  }
}
