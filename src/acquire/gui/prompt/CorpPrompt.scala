package acquire.gui.prompt

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment

import acquire.engine.Engine
import acquire.gui.Button
import acquire.gui.theatre.{Actor, World}

import scala.collection.mutable.ArrayBuffer

/**
  * A CorpPrompt is a prompt asking a user to select some number of corporations.
  * @param engine The game engine
  * @param message The message to display on the screen
  * @param corpChoices The choices of corps to checkout
  * @param canCheckoutCorp Can we checkout a corp based on what's currently checked out?
  */
class CorpPrompt(engine: Engine, val message: String, val corpChoices: Seq[Int],
                 val canCheckoutCorp: ((Int, Map[Int, Int]) => Boolean), val queueLike: Boolean) extends Actor {
  _height = 284
  _width = 388

  val submitButton: Button = new Button(50, 50, Color.web("0093ff"), Color.web("0093ff").darker().darker(), "OK")

  val corpButtons: ArrayBuffer[CorpButton] = corpChoices.map(corp => new CorpButton(engine, corp)).to[ArrayBuffer]
  val checkoutCorpButtons: ArrayBuffer[CorpButton] = ArrayBuffer()

  private def corpButtonPosition(num: Int) = (x + 23 + 50*num, y + 80)
  private def checkoutCorpButtonPosition(num: Int) = (x + 23 + 50*num, y + 150)

  override def addedToWorld(world: World): Unit = {
    world.addActor(submitButton, x+330, y+(284-58))
    corpButtons.indices.foreach(i => {
      val pos = corpButtonPosition(i)
      world.addActor(corpButtons(i), pos._1, pos._2)
    })
    for (button <- corpButtons) {
      button.registerClickHandler((Unit) => addCheckoutCorpButton(button.corp))
    }
  }

  override def removedFromWorld(world: World): Unit = {
    corpButtons.foreach(world.removeActor)
    checkoutCorpButtons.foreach(world.removeActor)
    world.removeActor(submitButton)
  }

  protected def addCheckoutCorpButton(corp: Int): Unit = {
    // TODO: cleanup
    if (!canCheckoutCorp(corp, selectedCorps)) {
      if (queueLike) {
        val truncatedCheckoutCorps = checkoutCorpButtons - checkoutCorpButtons.head
        val truncatedMap =
          for ((group, list) <- truncatedCheckoutCorps.map(_.corp).groupBy(i => i)) yield (group, list.length)
        if (canCheckoutCorp(corp, truncatedMap)) {
          if (worldOpt.isDefined)
            worldOpt.get.removeActor(checkoutCorpButtons.head)
          checkoutCorpButtons -= checkoutCorpButtons.head
        } else {
          return
        }
      } else {
        return
      }
    }

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
      submitButton.setPosition(x + 330, y + (284 - 58))
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
      setPosition(x, y) // refresh the position of the buttons.
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

    submitButton.draw(gc)
    corpButtons.foreach(_.draw(gc))
    checkoutCorpButtons.foreach(_.draw(gc))
  }
}
