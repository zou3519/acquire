package acquire.gui

import javafx.geometry.Rectangle2D
import javafx.scene.canvas.GraphicsContext

import acquire.engine.PlayerType.PlayerType
import acquire.engine.{PlayerType, Engine}
import acquire.state.{Config, AcquireState}



object Default {
  val VersionString = "0.3.0-α"
  private val corps = Seq("Tower", "Luxor", "American", "Worldwide", "Festival", "Imperial", "Continental").zip(
    Seq(200, 200, 300, 300, 300, 400, 400))
  private val playerNames: IndexedSeq[(String, PlayerType)] = Vector(
    ("p0", PlayerType.Human),
    ("p1", PlayerType.Ai),
    ("p2", PlayerType.Ai),
    ("p3", PlayerType.Ai))
  def newState = new AcquireState(new Config(playerNames.map(_._1), corps))
  def newEngine = new Engine(playerNames)
}
