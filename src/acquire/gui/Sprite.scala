package acquire.gui

import javafx.geometry.Rectangle2D
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

class Sprite extends Actor {
  private var _image: Image = null
//  private var _x: Double = 0
//  private var _y: Double = 0
//  private var _width: Double = 0
//  private var _height: Double = 0

  def setImage(image: Image) {
    _image = image
    _width = image.getWidth
    _height = image.getHeight
  }

  def setImage(filename: String) {
    setImage(new Image(filename))
  }

  override def draw(gc: GraphicsContext) {
    gc.drawImage(_image, _x, _y)
  }

  override def update(): Unit = ()

}
