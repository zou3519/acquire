package acquire.gui.prompt

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, TextAlignment}

import acquire.gui.theatre.{Actor, World}
import acquire.gui.{Button, Colors}

class NumberInput extends Actor {
  val up: Button = new Button(10,10,Colors.colors(3), Color.web("606060"), "▲")
  val down: Button = new Button(10,10,Colors.colors(2), Color.web("606060"), "▼")
  var value = 0

  _width = 45
  _height = 40
  up.registerClickHandler((Unit) => {
    value += 1
  })
  down.registerClickHandler((Unit) => {
    value -= 1
  })

  private val font: Font = Font.font("Arial", FontWeight.BOLD, 18)

  override def addedToWorld(world: World): Unit = {
    world.addActor(up, x+5, y+5)
    world.addActor(down, x+5, y + 20+5)
  }

  override def removedFromWorld(world: World): Unit = {
    world.removeActor(up)
    world.removeActor(down)
  }

  override def setPosition(x: Double, y: Double): Unit = {
    if (worldOpt.isDefined) {
      up.setPosition(x+5, y+5)
      down.setPosition(x+5, y+20+5)
    }
  }
  override def update(): Unit = {
    up.update()
    down.update()
  }

  override def draw(gc: GraphicsContext): Unit = {
    up.draw(gc)
    down.draw(gc)
    gc.setFill(Color.web("aaaaaa"))
    gc.setFont(font)
    gc.setTextAlign(TextAlignment.RIGHT)
    gc.setTextBaseline(VPos.CENTER)
    gc.fillText(value.toString, x + _width-5, y + _height/2)
  }
}
