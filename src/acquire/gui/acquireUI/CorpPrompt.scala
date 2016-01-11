package acquire.gui.acquireUI

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment

import acquire.engine.Engine
import acquire.gui.theatre.{ActorWithComponents, Actor, World}
import acquire.gui.theatreUI.Button

import scala.collection.mutable.ArrayBuffer

/**
  * A CorpPrompt is a prompt asking a user to select some number of corporations.
  * @param engine The game engine
  * @param message The message to display on the screen
  * @param corpChoices The choices of corps to checkout
  * @param canCheckoutCorp Can we checkout a corp based on what's currently checked out?
  */
class CorpPrompt(engine: Engine, val message: String, val corpChoices: Seq[Int],
                 val canCheckoutCorp: ((Int, Map[Int, Int]) => Boolean), val queueLike: Boolean) extends ActorWithComponents {

  val submitButton: Button = new Button(50, 50, Color.web("0093ff"), Color.web("0093ff").darker().darker(), "OK")
  val corpButtons: ArrayBuffer[CorpButton] = corpChoices.map(corp => new CorpButton(engine, corp)).to[ArrayBuffer]
  val checkoutCorpButtons: ArrayBuffer[CorpButton] = ArrayBuffer()

  init()

  private def init(): Unit = {
    _height = 284
    _width = 388
    addComponent(submitButton, 330, 284-58)
    for (i <- corpButtons.indices) {
      val pos = corpButtonPosition(i)
      val button = corpButtons(i)
      addComponent(button, pos._1, pos._2)
      button.registerClickHandler((Unit) => addCheckoutCorpButton(button.corp))
    }
  }

  def selectedCorps: Map[Int, Int] =
    for ((group, list) <- checkoutCorpButtons.map(_.corp).groupBy(i => i)) yield (group, list.length)

  protected def addCheckoutCorpButton(corp: Int): Unit = {
    canCheckoutCorp(corp, selectedCorps) match {
      // draw the button
      case true => addNewCheckoutButton(corp)

      case false if queueLike => // check to see if we're queuelike; if we are, then replace buttons
        val truncatedCheckoutCorps = checkoutCorpButtons - checkoutCorpButtons.head
        val truncatedMap = for ((group, list) <- truncatedCheckoutCorps.map(_.corp).groupBy(i => i))
          yield (group, list.length)
        if (canCheckoutCorp(corp, truncatedMap)) {
          removeComponent(checkoutCorpButtons.head)
          checkoutCorpButtons -= checkoutCorpButtons.head
          addNewCheckoutButton(corp)
        }

      case _ => ()
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

    super.draw(gc)
  }

  private def corpButtonPosition(num: Int) = (23 + 50*num, 80)
  private def checkoutCorpButtonPosition(num: Int) = (23 + 50*num, 150)

  private def updateCheckoutButtonsLocations() = {
    for (i <- checkoutCorpButtons.indices) {
      val pos = checkoutCorpButtonPosition(i)
      val button = checkoutCorpButtons(i)
      updateComponent(button, pos._1, pos._2)
    }
  }

  private def addNewCheckoutButton(corp: Int) = {
    val newButton: CorpButton = new CorpButton(engine, corp)
    checkoutCorpButtons += newButton
    val pos = checkoutCorpButtonPosition(checkoutCorpButtons.length - 1)
    addComponent(newButton, pos._1, pos._2)
    newButton.registerClickHandler((Unit) => {
      checkoutCorpButtons -= newButton
      removeComponent(newButton)
      updateCheckoutButtonsLocations()
    })
  }
}
