package acquire.gui.theatre

import javafx.scene.canvas.GraphicsContext

import scala.collection.mutable.ArrayBuffer

/**
  * An actor that can have components.
  * Components are actors that have locations relative to this actor.
  */
abstract class ActorWithComponents extends Actor {
  private val components: ArrayBuffer[Component] = ArrayBuffer()

  def addComponent(actor: Actor, xOffset: Double, yOffset: Double): Unit = {
    require(components.forall(_.actor != actor), "component already exists!")
    components += new Component(actor, xOffset, yOffset)
    if (worldOpt.isDefined)
      worldOpt.get.addActor(actor, x + xOffset, y + yOffset)
  }

  def updateComponent(actor: Actor, xOffset: Double, yOffset: Double): Unit = {
    val containsActor: ArrayBuffer[Component] = components.filter(_.actor == actor)
    require(containsActor.length == 1, "actor is not in the list of components")
    val component = containsActor.head
    component.xOffset = xOffset
    component.yOffset = yOffset
    if (actor.worldOpt.isDefined)
      actor.setPosition(x + xOffset, y + yOffset)
  }

  def removeComponent(actor: Actor): Unit = {
    val containsActor: ArrayBuffer[Component] = components.filter(_.actor == actor)
    require(containsActor.length == 1, "actor is not in the list of components")
    components -= containsActor.head
    if (worldOpt.isDefined)
      worldOpt.get.removeActor(actor)
  }

  override def addedToWorld(world: World): Unit = {
    super.addedToWorld(world)
    components.foreach(comp => world.addActor(comp.actor, x + comp.xOffset, y + comp.yOffset))
  }

  override def removedFromWorld(world: World): Unit = {
    super.addedToWorld(world)
    components.foreach(comp => world.removeActor(comp.actor))
  }

  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x, y)
    components.foreach(comp => comp.actor.setPosition(x + comp.xOffset, y + comp.yOffset))
  }

  override def update(): Unit = {
    super.update()
    val componentsCopy = components.clone
    componentsCopy.foreach(comp => comp.actor.update())
  }

  override def draw(gc: GraphicsContext): Unit = {
    super.draw(gc)
    val componentsCopy = components.clone
    componentsCopy.foreach(comp => comp.actor.draw(gc))
  }
}

sealed class Component private[theatre](val actor: Actor, var xOffset: Double, var yOffset: Double)
