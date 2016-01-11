package acquire.gui.prompt

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, TextAlignment}

import acquire.gui.theatre.{ActorWithComponents, Actor, World}
import acquire.gui.{Button, Colors}

class NumberInput extends ActorWithComponents {
  val up: Button = new Button(10,10,Colors.colors(3), Color.web("606060"), "▲")
  val down: Button = new Button(10,10,Colors.colors(2), Color.web("606060"), "▼")
  var value = 0

  init()

  private def init(): Unit = {
    _width = 45; _height = 40
    up.registerClickHandler((Unit) => value += 1)
    down.registerClickHandler((Unit) => value -= 1)
    addComponent(up, 5, 5)
    addComponent(down, 5, 25)
  }

  private val font: Font = Font.font("Arial", FontWeight.BOLD, 18)

  override def draw(gc: GraphicsContext): Unit = {
    gc.setFill(Color.web("aaaaaa"))
    gc.setFont(font)
    gc.setTextAlign(TextAlignment.RIGHT)
    gc.setTextBaseline(VPos.CENTER)
    gc.fillText(value.toString, x + _width-5, y + _height/2)
    super.draw(gc)
  }
}
