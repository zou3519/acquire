package acquire.gui

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.scene.transform.Affine

import acquire.engine.Engine

class ScoreSheet(engine: Engine) extends Actor {

  private def intOptToString(i: Option[Int]): String = i match {
    case None => "--"
    case Some(num) => num.toString
  }

  /* how to display a row, given a function that reveals some aspect of a corp */
  private def rowView(description: String, viewFn: (Int => Option[Int])): (Seq[Int] => Seq[String]) =
    (corps: Seq[Int]) => Seq(description) ++ corps.map(viewFn).map(intOptToString)

  private def playerRowView(player: Int): (Seq[Int] => Seq[String]) =
    (corps: Seq[Int]) => Seq(engine.state.config.playerName(player)) ++ corps.map {
      corp => engine.state.sheet.shares(corp,player).toString
    } ++ Seq((engine.state.sheet.cash(player)/100).toString, (engine.state.sheet.netWorth(player)/100).toString)

  private val viewFns: Vector[Seq[Int] => Seq[String]] =
    (for(player <- 0 until 4) yield playerRowView(player)).toVector ++ Vector(
    rowView("size", engine.state.sheet.chainSize),
    rowView("bank", corp => Some(engine.state.sheet.bankShares(corp))),
    rowView("price/100", engine.state.sheet.sharePrice(_).map(_/100)),
    rowView("major/100", engine.state.sheet.firstBonus(_).map(_/100)),
    rowView("minor/100", engine.state.sheet.secondBonus(_).map(_/100))
  )

  private def rows: Seq[Seq[String]] = viewFns.map(fn => fn(engine.state.config.corps))

  val cellWidth = 34
  val cellHeight = 28

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
        gc.setFill(Colors.colors(0))

        // text highlighting for the players
        if (j == 4) {
          if (row(i).forall(_.isDigit) && row(i).toInt >= 11) {
            gc.setFill(Colors.colors(3)) // green
          }
        }
        gc.fillText(row(i), x+i*cellWidth + cellWidth/4, y+j*cellHeight+80+cellHeight)
      }
    }
  }
}
