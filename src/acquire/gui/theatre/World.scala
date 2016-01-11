package acquire.gui.theatre

import javafx.scene.canvas.GraphicsContext

import scala.collection.mutable.ArrayBuffer

/**
  * A world contains actors. The world will update each actor individually and draw the actors
  * onto a GraphicsContext object.
  */
class World {
  val actors: ArrayBuffer[Actor] = ArrayBuffer()

  /**
    * Add an actor to this world.
    * @param actor The actor to be added
    * @param x The x-position to add the actor
    * @param y The y-position to add the actor
    */
  def addActor(actor: Actor, x: Double, y: Double): Unit = {
    require(!actors.contains(actor), "actor has already been added!")
    actors += actor
    actor._worldOpt = Some(this)
    actor._x = x
    actor._y = y
    actor.addedToWorld(this)
    actor.setPosition(x, y)
  }

  /**
    * Remove an actor from this world
    * @param actor The actor to be removed
    */
  def removeActor(actor: Actor): Unit = {
    require(actors.contains(actor), "no actor in world!")
    actor.removedFromWorld(this)
    actor._worldOpt = None
    actors -= actor
  }

  /**
    * Call all of the actor's update functions.
    */
  def updateActors(): Unit = {
    val actorsIterator = actors.clone.toIterator
    while (actorsIterator.hasNext) {
      val actor = actorsIterator.next()
      if (actor.worldOpt.isDefined) actor.update()
    }
  }

  /**
    * Draw the actors
    * @param gc The GraphicsContext to draw the actors onto
    */
  def drawActors(gc: GraphicsContext): Unit = {
    val actorsIterator = actors.clone.toIterator
    while (actorsIterator.hasNext) {
      val actor = actorsIterator.next()
      if (actor.worldOpt.isDefined) actor.draw(gc)
    }
  }

}
