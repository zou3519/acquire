package acquire.gui

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.{TextAlignment, FontWeight, Font}

import acquire.engine.{PlayerType, Engine}
import acquire.state._

class TilePiece(engine: Engine, val row: Int, val col: Int) extends ClickableActor {
  private val loc: String = Locations.Store(row)(col).toString
  private val size: Int = 48
  private val font: Font = Font.font("Arial", FontWeight.BOLD, 14)
  _width = 50
  _height = 50

  private var tileColor: Color = Colors.colors(0)
  private var empty: Boolean = tileColor == Colors.colors(0)
  private var corpName = ""
  private var _highlighted: Boolean = false

  def highlighted: Boolean = _highlighted

  def setType(tile: Tile): Unit = {
    empty = tile.isInstanceOf[EmptyTile]
    tileColor = Colors.colorOf(tile)
    tile match {
      case CorpTile(id) => corpName = engine.state.config.corpName(id)
      case _ => ()
    }
  }

  override def update(): Unit = {
    super.update()
    setType(engine.state.board.tileAt(row,col))
    if (engine.state.tileRack(engine.state.currentPlayer).contains(Locations.Store(row)(col))) {
      _highlighted = true
    } else {
      _highlighted = false
    }
  }


  override def draw(gc: GraphicsContext): Unit = {
    if (empty) {
      if (_highlighted) {
        if (MouseUtil.mouseOver(this)) {
          gc.setFill(tileColor)
        } else {
          gc.setFill(tileColor.darker())
        }
        gc.fillRoundRect(_x+1, _y+1, size, size, 6, 6)
      }

      val stroke = 2.0
      gc.setLineWidth(stroke)
      if (_highlighted) gc.setStroke(tileColor.brighter())
      else gc.setStroke(tileColor)
      gc.strokeRoundRect(_x+1+stroke/4, _y+1+stroke/4, size-stroke/2, size-stroke/2, stroke, stroke)

      gc.setFont(font)
      gc.setTextAlign(TextAlignment.CENTER)
      gc.setTextBaseline(VPos.CENTER)
      if (_highlighted) gc.setFill(tileColor.brighter())
      else gc.setFill(tileColor)
      gc.fillText(loc, _x + _width/2, _y + _height/2)
    } else {
      gc.setFill(tileColor)
      gc.fillRoundRect(_x+1, _y+1, size, size, 6, 6)

//      if (corpName != "") {
//        gc.setFont(font)
//        gc.setTextAlign(TextAlignment.CENTER)
//        gc.setTextBaseline(VPos.CENTER)
//        gc.setFill(tileColor.darker().darker())
//        gc.fillText(corpName.charAt(0).toString, _x + _width/2, _y + _height/2)
//      }
    }
  }
}
