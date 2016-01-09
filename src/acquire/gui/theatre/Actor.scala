package acquire.gui.theatre

import javafx.geometry.Rectangle2D
import javafx.scene.canvas.GraphicsContext

import acquire.gui.theatre.World

/**
  * An actor is something that is drawable on a canvas
  */
trait Actor {
  private[theatre] var _worldOpt: Option[World] = None
  private[theatre] var _x: Double = 0
  private[theatre] var _y: Double = 0

  protected var _width: Double = 0
  protected var _height: Double = 0

  def x: Double = _x
  def y: Double = _y
  def worldOpt: Option[World] = _worldOpt

  /**
    * Called when the actor is added to the world.
    * @param world the world this actor is being added to
    */
  def addedToWorld(world: World): Unit = ()

  /**
    * Called when the actor is removed from the world.
    * @param world The world the actor is being removed from
    */
  def removedFromWorld(world: World): Unit = ()

  def setPosition(x: Double, y: Double) {
    require(_worldOpt.isDefined, "actor must be added to a world first!")
    _x = x
    _y = y
  }

  def boundary: Rectangle2D =
    new Rectangle2D(_x, _y, _width, _height)

  def update(): Unit = ()
  def draw(gc: GraphicsContext): Unit = ()
}
