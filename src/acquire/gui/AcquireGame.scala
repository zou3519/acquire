package acquire.gui

import acquire.engine.{PlayerType, Engine}
import acquire.state._

/**
  * AcquireGame is an actor! It can't be drawn, though
  */
class AcquireGame(engine: Engine, guiBoard: Board, guiScoreSheet: ScoreSheet) extends Actor {
  var count = 0
  private var hasSetupHumanMove = false

  // game logic here
  override def update(): Unit = {
    if (!engine.state.isOver) {
      engine.currentPlayerType match {
        case PlayerType.Human => if (!hasSetupHumanMove) setupHumanMove()
        case PlayerType.Ai => aiMove()
      }
    }
  }

  private def setupHumanPlaceTile() = {
    val currentPlayerTiles = engine.state.tileRack(engine.state.currentPlayer)
    val physicalTiles = for (tile <- currentPlayerTiles) yield guiBoard.tiles(tile.row)(tile.col)

    for (tile <- physicalTiles) {
      val move = PlaceTile(engine.state.currentPlayer, Locations.Store(tile.row)(tile.col))
      if (engine.state.isLegalMove(move))
        tile.registerClickHandler((Unit) => {
          if (tile.highlighted) {
            engine.makeMove(move)
            for (physicalTile <- physicalTiles) { physicalTile.removeClickHandler() }
            hasSetupHumanMove = false
          }
        })
    }
  }

  private def setupHumanBuyShares() = {
    val prompt: CorpPrompt = new BuySharesPrompt(engine)
    worldOpt.get.addActor(prompt, 624, 470)
    prompt.submitButton.registerClickHandler((Unit) => {
      val move: Move = BuyShares(engine.state.currentPlayer, prompt.selectedCorps)
      engine.makeMove(move)
      worldOpt.get.removeActor(prompt)
      hasSetupHumanMove = false
    })
  }

  private def setupHumanMove(): Unit = {
    require(!hasSetupHumanMove)
    hasSetupHumanMove = true

    engine.state.expectedMoveType match {
      case MoveType.EndTurnT => aiMove(); hasSetupHumanMove = false
      case MoveType.PlaceTileT => setupHumanPlaceTile()
      case MoveType.FoundCorpT => aiMove(); hasSetupHumanMove = false
      case MoveType.MergeCorpT => aiMove(); hasSetupHumanMove = false
      case MoveType.MergeTransactionT => aiMove(); hasSetupHumanMove = false
      case MoveType.BuySharesT => setupHumanBuyShares()
    }
  }

  private def aiMove(): Unit = {
    // ai's make moves at 1 per second
    if (count % 1 == 0) {
      engine.makeMove(engine.state.randomMove.get)
      println(engine.state.prettyPrint)
    }
    count += 1
  }

}
