package acquire.gui

import javafx.geometry.Rectangle2D
import javafx.scene.canvas.GraphicsContext

import acquire.engine.PlayerType.PlayerType
import acquire.engine.{PlayerType, Engine}
import acquire.state.{Config, AcquireState}

/**
  * An actor is something that is drawable on a canvas
  */
trait Actor {
  // TODO: hide
  var _x: Double = 0
  var _y: Double = 0
  protected var _width: Double = 0
  protected var _height: Double = 0

  // todo... hide
  var _worldOpt: Option[World] = None

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
    * @param world
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

object Default {
  val VersionString = "0.1.1-α"
  private val corps = Seq("Tower", "Luxor", "American", "Worldwide", "Festival", "Imperial", "Continental").zip(
    Seq(200, 200, 300, 300, 300, 400, 400))
  private val playerNames: IndexedSeq[(String, PlayerType)] = Vector(
    ("p0", PlayerType.Human),
    ("p1", PlayerType.Ai),
    ("p2", PlayerType.Ai),
    ("p3", PlayerType.Ai))
  def newState = new AcquireState(new Config(playerNames.map(_._1), corps))
  def newEngine = new Engine(playerNames)
}
