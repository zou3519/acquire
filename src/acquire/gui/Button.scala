package acquire.gui

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.{FontWeight, Font, TextAlignment}

import acquire.gui.ClickableActor


class Button(width: Double, height: Double, buttonFill: Color, textFill: Color,
                      textString: String) extends ClickableActor {
  _width = width
  _height = height
  type DrawFunction = ((GraphicsContext, Double, Double) => Unit) // (gc, x, y) => Unit

  private val font: Font = Font.font("Arial", FontWeight.BOLD, 14)

  private val drawRect: DrawFunction = (gc, x, y) =>
    gc.fillRoundRect(x, y, _width, _height, 6, 6)
  private val drawOutline: DrawFunction = (gc, x, y) =>
    gc.strokeRoundRect(x, y, _width, _height, 6, 6)
  private val drawText: DrawFunction = (gc, x, y) => {
    gc.setFont(font)
    gc.setTextAlign(TextAlignment.CENTER)
    gc.setTextBaseline(VPos.CENTER)
    gc.fillText(textString, x + _width/2, y + _width/2)
  }

  private def drawButton(rectColor: Color, outlineColor: Color, textColor: Color): DrawFunction = (gc, x, y) => {
    gc.setFill(rectColor)
    drawRect(gc, x, y)
    gc.setLineWidth(5)
    gc.setStroke(outlineColor)
    drawOutline(gc, x, y)
    gc.setFill(textColor)
    drawText(gc, x, y)
  }

  private def drawRegular(gc: GraphicsContext, x: Double, y: Double): Unit =
    drawButton(buttonFill, buttonFill, textFill)(gc, x, y)
  private def drawOnHover(gc: GraphicsContext, x: Double, y: Double): Unit =
    drawButton(buttonFill, buttonFill.darker(), textFill)(gc, x, y)
  private def drawOnMouseDown(gc: GraphicsContext, x: Double, y: Double): Unit =
    drawButton(buttonFill.darker, buttonFill.darker(), textFill)(gc, x, y)

  override def update(): Unit = {
    super.update()
  }

  override def draw(gc: GraphicsContext): Unit = {
    super.draw(gc)
    (MouseUtil.mouseOver(this), MouseUtil.isMousePressed) match {
      case (true, true) => drawOnMouseDown(gc, x, y)
      case (true, false) => drawOnHover(gc, x, y)
      case _ => drawRegular(gc, x, y)
    }
  }
}
