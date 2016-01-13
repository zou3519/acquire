package theatre.core

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.{Node, Scene, Group}
import javafx.scene.canvas.{Canvas, GraphicsContext}


import scala.collection.mutable.ArrayBuffer

/**
  * A world contains actors and is a wrapper around a Scene and a Canvas.
  * Actors live in the Canvas while JavaFx graph nodes may be added to the Scene
  * The world will update each actor individually and draw the actors on
  * the scene's main canvas.
  */
abstract class World(width: Double, height: Double) {
  val actors: ArrayBuffer[Actor] = ArrayBuffer()

  private val sceneRoot: Group = new Group()
  val scene: Scene = new Scene(sceneRoot)
  val canvas = new Canvas(width, height)
  val gc = canvas.getGraphicsContext2D

  var theatre: Option[Theatre] = None

  init()

  private def init(): Unit = {
    addNode(canvas)
    scene.getStylesheets.add("acquire/gui/stylesheet.css")
    setupMouseEvents()
    buildWorld()
  }

  /**
    * Override this to build the World in a subclass
    */
  def buildWorld(): Unit = ()

  /**
    * Add a Scene Node to the world's Scene
    * @param node The node to be added
    */
  def addNode(node: Node): Unit = {
    sceneRoot.getChildren.add(node)
  }

  /**
    * Remove a Scene Node from this world's Scene
    * @param node The node to be added
    */
  def removeNode(node: Node): Unit = {
    sceneRoot.getChildren.remove(node)
  }

  /**
    * Add an actor to the world's canvas
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
  def updateWorld(): Unit = {
    val actorsIterator = actors.clone.toIterator
    while (actorsIterator.hasNext) {
      val actor = actorsIterator.next()
      if (actor.worldOpt.isDefined) actor.update()
    }
  }

  /**
    * Draw the actors
    */
  def drawWorld(): Unit = {
    drawWorldBackground()
    val actorsIterator = actors.clone.toIterator
    while (actorsIterator.hasNext) {
      val actor = actorsIterator.next()
      if (actor.worldOpt.isDefined) actor.draw(gc)
    }
  }

  /**
    * Draw the world's background
    */
  def drawWorldBackground(): Unit = ()

  /**
    * Registers mouse events on the scene to be accessed using MouseUtil
    */
  private def setupMouseEvents(): Unit = {
    def mouseEventHandler(handler: (MouseEvent => Unit)): EventHandler[MouseEvent] = {
      new EventHandler[MouseEvent] {
        def handle(event: MouseEvent) = handler(event)
      }
    }
    scene.setOnMouseEntered(mouseEventHandler(event => MouseUtil.isMouseInScene = true))
    scene.setOnMouseExited(mouseEventHandler(event => MouseUtil.isMouseInScene = false))
    scene.setOnMouseClicked(mouseEventHandler(event => {
      MouseUtil.isMouseClicked = true
      MouseUtil.setClickPosition(event.getSceneX, event.getSceneY)
    }))
    scene.setOnMousePressed(mouseEventHandler(event => MouseUtil.isMousePressed = true))
    scene.setOnMouseReleased(mouseEventHandler(event => MouseUtil.isMousePressed = false))
    scene.setOnMouseMoved(mouseEventHandler(event => MouseUtil.setPosition(event.getSceneX, event.getSceneY)))
  }
}
