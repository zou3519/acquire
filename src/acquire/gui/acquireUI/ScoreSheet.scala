package acquire.gui.acquireUI

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.scene.transform.Affine

import acquire.engine.Engine
import acquire.gui.Colors
import acquire.state.Shareholder
import theatre.core.Actor

class ScoreSheet(engine: Engine) extends Actor {

  private def intOptToString(i: Option[Int]): String = i match {
    case None => "--"
    case Some(num) => num.toString
  }

  /* how to display a row, given a function that reveals some aspect of a corp */
  private def rowView(description: String, viewFn: (Int => Option[Int]),
                      colorFn: (Option[Int] => Color)): (Seq[Int] => Seq[(String, Color)]) =
    (corps: Seq[Int]) =>
      Seq( (description, Colors.colors(0))) ++ corps.map(viewFn).map(
      (elt: Option[Int]) => (intOptToString(elt), colorFn(elt)) )

  private def playerRowView(player: Int): (Seq[Int] => Seq[(String, Color)]) =
    (corps: Seq[Int]) => (Seq((engine.state.config.playerName(player), Colors.colors(0))) ++ corps.map {
      corp => {
        val majHolders = engine.state.topShareholders(corp)(Shareholder.Majority)
        val minHolders = engine.state.topShareholders(corp)(Shareholder.Minority)
        (engine.state.sheet.shares(corp,player).toString,
          if (majHolders contains player)
            Colors.colors(3)
          else if (majHolders.length <= 1 && minHolders.contains(player))
            Colors.colors(2)
          else
            Colors.colors(0))
      }
    }) ++
      Seq( ((engine.state.sheet.cash(player)/100).toString, Colors.colors(0)),  // player cash
        ((engine.state.sheet.netWorth(player)/100).toString, Colors.colors(0))) // player net worth

  private val viewFns: Vector[Seq[Int] => Seq[(String, Color)]] =
    (for(player <- 0 until 4) yield playerRowView(player)).toVector ++ Vector(
    rowView("size", corp => engine.state.sheet.chainSize(corp),
      (elt) => if (elt.getOrElse(0) >= 11) Colors.colors(3) else Colors.colors(0)),
    rowView("bank", corp => Some(engine.state.sheet.bankShares(corp)),
      (elt) => if (elt.getOrElse(1) > 0) Colors.colors(0) else Colors.colors(2)),
    rowView("price/100", engine.state.sheet.sharePrice(_).map(_/100), (elt) => Colors.colors(0)),
    rowView("major/100", engine.state.sheet.firstBonus(_).map(_/100), (elt) => Colors.colors(0)),
    rowView("minor/100", engine.state.sheet.secondBonus(_).map(_/100), (elt) => Colors.colors(0))
  )

  private def rows: Seq[Seq[(String, Color)]] = viewFns.map(fn => fn(engine.state.config.corps))

  val cellWidth = 34
  val cellHeight = 28

  // TODO: clean up the above... it looks unwieldy
  override def draw(gc: GraphicsContext): Unit = {
    val corps = Vector("◄ Tower", "◄ Luxor", "◄ American", "◄ Worldwide", "◄ Festival", "◄ Imperial", "◄ Continental", "cash/100", "net worth/100")

    // draw corp names
    val at = new Affine()
    at.appendRotation(-90)
    gc.setTransform(at)
    gc.setTextAlign(TextAlignment.LEFT)
    for (i <- corps.indices) {
      gc.setFill(Colors.colors( (i+2)%Colors.colors.size))
      gc.fillText(corps(i), -y-80, x + cellWidth*i + cellWidth)
    }
    at.appendRotation(90)
    gc.setTransform(at)

    gc.setFill(Colors.colors(0))
    gc.setTextAlign(TextAlignment.RIGHT)
    for (j <- rows.indices) {
      val row = rows(j)
      for (i <- row.indices) {
        val (text, color) = row(i)
        gc.setFill(color)
        gc.fillText(text, x+i*cellWidth + cellWidth/4, y+j*cellHeight+80+cellHeight)
      }
    }
  }
}
