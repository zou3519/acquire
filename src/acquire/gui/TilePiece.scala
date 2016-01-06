package acquire.gui

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.{TextAlignment, FontWeight, Font}

import acquire.state._

class TilePiece(state: AcquireState, row: Int, col: Int) extends Actor {
  private val loc: String = Locations.Store(row)(col).toString
  private val size: Int = 48
  private val font: Font = Font.font("Arial", FontWeight.BOLD, 14)
  _width = 50
  _height = 50

  private var tileColor: Color = Colors.colors(0)
  private var empty: Boolean = tileColor == Colors.colors(0)
  private var corpName = ""
  private var highlighted: Boolean = false

  def setType(tile: Tile): Unit = {
    empty = tile.isInstanceOf[EmptyTile]
    tileColor = Colors.colorOf(tile)
    if (tile.isInstanceOf[CorpTile]) {
      corpName = state.config.corpName(tile.asInstanceOf[CorpTile].corpId)
    }
  }

  override def update(): Unit = {
    setType(state.board.tileAt(row,col))
    if (state.tileRack(state.currentPlayer).contains(Locations.Store(row)(col))) {
      highlighted = true
    } else {
      highlighted = false
    }
    if (highlighted && MouseUtil.mouseClicked(this)) {
      println("clicked " + row + "," + col)
    }
  }


  override def draw(gc: GraphicsContext): Unit = {
    if (empty) {
      if (highlighted) {
        if (MouseUtil.mouseOver(this)) {
          gc.setFill(tileColor)
        } else {
          gc.setFill(tileColor.darker())
        }
        gc.fillRoundRect(_x+1, _y+1, size, size, 6, 6)
      }

      val stroke = 2.0
      gc.setLineWidth(stroke)
      if (highlighted) gc.setStroke(tileColor.brighter())
      else gc.setStroke(tileColor)
      gc.strokeRoundRect(_x+1+stroke/4, _y+1+stroke/4, size-stroke/2, size-stroke/2, stroke, stroke)

      gc.setFont(font)
      gc.setTextAlign(TextAlignment.CENTER)
      gc.setTextBaseline(VPos.CENTER)
      if (highlighted) gc.setFill(tileColor.brighter())
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
