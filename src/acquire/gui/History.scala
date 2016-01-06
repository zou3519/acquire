package acquire.gui

import javafx.collections.{ObservableList, FXCollections}
import javafx.scene.Group
import javafx.scene.control.ListView

import acquire.engine.{EngineDefaults, Engine}

import scala.collection.JavaConverters

class History(val engine: Engine) extends Actor {
  private val list: ListView[String] = new ListView[String]
  private val items: ObservableList[String] = FXCollections.observableArrayList("GAME START")
  _width = 600
  _height = 284
  list.setItems(items)
  list.setPrefSize(_width, _height)

  private val currentNumMoves = 0

  def addToGroup(root: Group): Unit =
    root.getChildren.add(list)

  override def update(): Unit = {
    if (currentNumMoves != engine.numMoves) {
      val flatList: java.util.List[String] =
        JavaConverters.seqAsJavaListConverter(engine.flatHistory.map(
          moveRecord => EngineDefaults.moveToString(moveRecord.move))).asJava
      list.setItems(FXCollections.observableList[String](flatList))
    }
  }

  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x,y)
    list.setTranslateX(x)
    list.setTranslateY(y)
  }
}
