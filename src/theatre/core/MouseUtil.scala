package theatre.core

object MouseUtil {
  var mouseX: Double = .0
  var mouseY: Double = .0
  var clickX: Double = .0
  var clickY: Double = .0
  var isMouseInScene: Boolean = false
  var isMouseClicked: Boolean = false
  var isMousePressed: Boolean = false

  def setPosition(x: Double, y: Double) {
    mouseX = x
    mouseY = y
  }

  def setClickPosition(x: Double, y: Double) {
    clickX = x
    clickY = y
  }

  def mouseOver(actor: Actor): Boolean =
    actor.boundary.contains(mouseX, mouseY)

  def mouseClicked(actor: Actor): Boolean = {
    if (isMouseClicked && actor.boundary.contains(clickX, clickY)) {
      isMouseClicked = false
      true
    }
    else {
      false
    }
  }
}
