package acquire.gui.acquireUI

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.text.{Font, FontWeight, TextAlignment}

import acquire.gui.Colors
import acquire.gui.theatre.{Actor, World}

class ThinkingMessage(player: String, timerMillis: Int) extends Actor {

  private val font: Font = Font.font("Helvetica", FontWeight.BOLD, 18)
  private var addedTimeMillis: Long = 0
  private var time: String = millisToString(timerMillis)

  override def addedToWorld(world: World) = {
    addedTimeMillis = System.currentTimeMillis
  }

  def millisToString(millis: Long): String =
    f"${millis.toDouble/1000}%2.3fs"

  override def update(): Unit = {
    val elapsed = System.currentTimeMillis - addedTimeMillis
    val remaining: Long = Math.max(timerMillis - elapsed, 0)
    time = millisToString(remaining)
  }

  override def draw(gc: GraphicsContext): Unit = {
    gc.setFont(font)
    gc.setFill(Colors.colors(0))
    gc.setTextAlign(TextAlignment.CENTER)
    gc.setTextBaseline(VPos.CENTER)
    gc.fillText(player + " is thinking...", x, y)
    gc.fillText(time, x, y+25)
  }
}
