package acquire.gui.theatre

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.stage.Stage

/**
  * A Theatre is an abstract for a JavaFx application.
  * The Theatre holds one world at a time, with each world containing a JavaFx scene graph
  * and a Canvas. Actors live on the Canvas and can be updated and drawn.
  */
abstract class Theatre extends Application {
  private var _world: World = null
  private var _worldThread: AnimationTimer = null
  private var stage: Stage = null

  override def start(stage: Stage): Unit = {
    this.stage = stage
    perform()
    stage.show()
  }

  /**
    * Override this method to display a world in the theatre
    */
  def perform(): Unit = ()

  /**
    * Set the world as the current displaying world
    * @param world The world to display
    */
  def setWorld(world: World): Unit = {
    stage.setScene(world.scene)
    _world = world

    if (_worldThread != null)
      _worldThread.stop()

    _worldThread = createAnimationTimerFor(world)
    _worldThread.start()
  }

  /**
    * Creates an animation timer for a world.
    * The animation timer is responsible for updating & drawing the world.
    * @param world The world to create a timer for
    * @return The animation timer
    */
  private def createAnimationTimerFor(world: World): AnimationTimer = {
    new AnimationTimer {
      override def handle(currentNanoTime: Long): Unit = {
        world.updateWorld()
        world.drawWorld()
      }
    }
  }
}
