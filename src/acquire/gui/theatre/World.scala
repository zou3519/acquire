package acquire.gui.theatre

import javafx.scene.canvas.GraphicsContext

import scala.collection.mutable.ArrayBuffer

/**
  * A world contains actors. The world will update each actor individually and draw the actors
  * onto a GraphicsContext object.
  */
class World {

  val actors: ArrayBuffer[Actor] = ArrayBuffer()

  def addActor(actor: Actor, x: Double, y: Double): Unit = {
    require(!actors.contains(actor), "actor has already been added!")
    actors += actor
    actor._worldOpt = Some(this)
    actor._x = x
    actor._y = y
    actor.addedToWorld(this)
    actor.setPosition(x, y)
  }

  def removeActor(actor: Actor): Unit = {
    require(actors.contains(actor), "no actor in world!")
    actor.removedFromWorld(this)
    actor._worldOpt = None
    actors -= actor
  }

  /**
    * updateActors() has to be defended against actors removing their siblings.
    * We create a copy of the list then update everyone as necessary.
    */
  def updateActors(): Unit = {
    val actorsIterator = actors.clone.toIterator
    while (actorsIterator.hasNext) {
      val actor = actorsIterator.next()
      if (actor.worldOpt.isDefined) actor.update()
    }
  }

  def drawActors(gc: GraphicsContext): Unit = {
    val actorsIterator = actors.clone.toIterator
    while (actorsIterator.hasNext) {
      val actor = actorsIterator.next()
      if (actor.worldOpt.isDefined) actor.draw(gc)
    }
  }

}
