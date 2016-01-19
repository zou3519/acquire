package acquire.gui.acquireUI

import javafx.collections.{FXCollections, ObservableList}
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.scene.text.Text

import acquire.engine.Engine
import acquire.gui.Colors
import theatre.core.{World, Actor}

import scala.collection.JavaConverters

class History(val engine: Engine) extends Actor {
  private val list: ListView[FlowPane] = new ListView[FlowPane]
  private val items: ObservableList[FlowPane] = FXCollections.observableArrayList(new FlowPane)

  init()

  private def init(): Unit = {
    _width = 600
    _height = 284
    list.setItems(items)
    list.setPrefSize(_width, _height)
  }

  private var currentNumMoves = 0

  override def addedToWorld(world: World) = {
    super.addedToWorld(world)
    world.addNode(list)
  }

  override def removedFromWorld(world: World) = {
    super.removedFromWorld(world)
    world.removeNode(list)
  }

  override def update(): Unit = {
    if (currentNumMoves != engine.numMoves) {
      val moveList: java.util.List[FlowPane] =
        JavaConverters.seqAsJavaListConverter(engine.flatHistory.flatMap(
          moveRecord => styleText(moveRecord.description))).asJava
      list.setItems(FXCollections.observableList[FlowPane](moveList))
      currentNumMoves = engine.numMoves
      // hardcoding 13 to avoid what seems like a JavaFx bug
      if (currentNumMoves >= 13)
        list.scrollTo(currentNumMoves - 1)
    }
  }

  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x,y)
    list.setTranslateX(x)
    list.setTranslateY(y)
  }

  private def styleText(input: String): Seq[FlowPane] = {
    val lines = input.trim().split("\n")
    lines.map(styleLine)
  }

  private def styleLine(input: String): FlowPane = {
    val result: FlowPane = new FlowPane
    val wordList = input.split("\\b")
    for (word <- wordList)
      result.getChildren.add(styleWord(word))
    result.setAlignment(Pos.CENTER)
    result
  }

  private def styleWord(input: String): Text = {
    val corpNames = engine.config.corps.map(engine.config.corpName)
    if (corpNames.contains(input))
      customizeWord(input, Colors.colors(2 + corpNames.indexOf(input)))
    else
      customizeWord(input, Color.web("aaaaaa"))
  }

  private def customizeWord(word: String, color: Color): Text = {
    val text: Text = new Text(word)
    text.setFill(color)
    text
  }
}
