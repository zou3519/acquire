package acquire.gui.acquireUI

import acquire.gui.newGameScreen.NewGameWorld
import theatre.core.Theatre

class AcquireTheatre extends Theatre {

  override def perform(): Unit = {
    //val engine: Engine = Default.newEngine
    //val world: AcquireWorld = new AcquireWorld(engine)
    setTitle("Acquire")
    val world = new NewGameWorld()
    setWorld(world)
  }
}
