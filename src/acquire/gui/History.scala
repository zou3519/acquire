package acquire.gui

import javafx.collections.{ObservableList, FXCollections}
import javafx.scene.Group
import javafx.scene.control.ListView

class History extends Actor {
  private val list: ListView[String] = new ListView[String]
  private val items: ObservableList[String] = FXCollections.observableArrayList(
    "formed tower formed tower formed tower formed tower formed tower formed tower formed tower formed tower formed tower formed tower",
    "formed luxor", "formed american", "formed worldwide", "formed tower", "formed luxor", "formed american",
    "formed worldwide", "formed tower", "formed luxor", "formed american", "formed worldwide", "formed tower",
    "formed luxor", "formed american", "formed worldwide", "formed tower", "formed luxor", "formed american",
    "formed worldwide", "formed tower", "formed luxor", "formed american", "formed worldwide", "formed tower",
    "formed luxor", "formed american", "formed worldwide")
  _width = 600
  _height = 284
  list.setItems(items)
  list.setPrefSize(_width, _height)

  def addToGroup(root: Group): Unit =
    root.getChildren.add(list)

  override def setPosition(x: Double, y: Double): Unit = {
    super.setPosition(x,y)
    list.setTranslateX(x)
    list.setTranslateY(y)
  }
}
