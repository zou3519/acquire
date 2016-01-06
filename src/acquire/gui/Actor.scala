package acquire.gui

import javafx.geometry.Rectangle2D
import javafx.scene.canvas.GraphicsContext

import acquire.engine.Engine
import acquire.state.{Config, AcquireState}

/**
  * An actor is something that is drawable on a canvas
  */
trait Actor {
  protected var _x: Double = 0
  protected var _y: Double = 0
  protected var _width: Double = 0
  protected var _height: Double = 0

  def x: Double = _x
  def y: Double = _y

  def setPosition(x: Double, y: Double) {
    _x = x
    _y = y
  }

  def boundary: Rectangle2D =
    new Rectangle2D(_x, _y, _width, _height)

  def update(): Unit = ()
  def draw(gc: GraphicsContext): Unit = ()
}

object Default {
  private val corps = Seq("Tower", "Luxor", "American", "Worldwide", "Festival", "Imperial", "Continental").zip(
    Seq(200, 200, 300, 300, 300, 400, 400))
  private val playerNames = Seq("p0.alpha", "p1.beta","p2.gamma","p3.delta")
  def newState = new AcquireState(new Config(playerNames, corps))
  def newEngine = new Engine(playerNames)
}
