package acquire.gui.acquireUI

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, TextAlignment}

import acquire.engine.{Engine, PlayerType}
import acquire.gui.Colors
import theatre.core.{ClickableActor, MouseUtil}
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
      case CorpTile(id) => {
        val corpHQ = engine.corpHQ(id)
        if (corpHQ.nonEmpty)
          if (corpHQ.get.row == row && corpHQ.get.col == col)
            corpName = new String(engine.state.config.corpName(id).toCharArray.take(2))
          else corpName = ""
      }
      case _ => ()
    }
  }

  override def update(): Unit = {
    super.update()
    setType(engine.state.board.tileAt(row,col))
    if (engine.currentPlayerType == PlayerType.Human && engine.state.tileRack(engine.state.currentPlayer).contains(Locations.Store(row)(col))) {
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
        gc.fillRoundRect(x+1, y+1, size, size, 6, 6)
      }

      val stroke = 2.0
      gc.setLineWidth(stroke)
      if (_highlighted) gc.setStroke(tileColor.brighter())
      else gc.setStroke(tileColor)
      gc.strokeRoundRect(x+1+stroke/4, y+1+stroke/4, size-stroke/2, size-stroke/2, stroke, stroke)

      gc.setFont(font)
      gc.setTextAlign(TextAlignment.CENTER)
      gc.setTextBaseline(VPos.CENTER)
      if (_highlighted) gc.setFill(tileColor.brighter())
      else gc.setFill(tileColor)
      gc.fillText(loc, x + _width/2, y + _height/2)
    } else {
      gc.setFill(tileColor)
      gc.fillRoundRect(x+1, y+1, size, size, 6, 6)

      gc.setFont(font)
      gc.setTextAlign(TextAlignment.CENTER)
      gc.setTextBaseline(VPos.CENTER)
      gc.setFill(Color.web("707070"))
      gc.fillText(corpName, x+ _width/2, y + _height/2)
    }
  }
}
