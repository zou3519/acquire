package acquire.gui

import javafx.application.Platform
import javafx.collections.{ObservableList, FXCollections}
import javafx.scene.Group
import javafx.scene.control.ListView

import acquire.engine.{MoveRecord, EngineDefaults, Engine}
import acquire.gui.theatre.Actor

import scala.collection.JavaConverters

class History(val engine: Engine) extends Actor {
  private val list: ListView[String] = new ListView[String]
  private val items: ObservableList[String] = FXCollections.observableArrayList("GAME START")
  _width = 600
  _height = 284
  list.setItems(items)
  list.setPrefSize(_width, _height)
//  Platform.runLater( new Runnable{
//    override def run(): Unit =
//  })

  private val currentNumMoves = 0

  def addToGroup(root: Group): Unit =
    root.getChildren.add(list)

  override def update(): Unit = {
    if (currentNumMoves != engine.numMoves) {
      val flatList: java.util.List[String] =
        JavaConverters.seqAsJavaListConverter(engine.flatHistory.map(
          moveRecord => EngineDefaults.moveToString(moveRecord.move))).asJava
      list.setItems(FXCollections.observableList[String](flatList))
//      list.scrollTo(list.getItems.size()-1) // TODO: debug
    }
  }

  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x,y)
    list.setTranslateX(x)
    list.setTranslateY(y)
  }
}
