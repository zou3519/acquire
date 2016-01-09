package acquire.gui

import acquire.engine.{PlayerType, Engine}
import acquire.gui.prompt.{MergeTransactionPrompt, CorpPrompt}
import acquire.gui.theatre.Actor
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
    val prompt: CorpPrompt = createBuySharesPrompt
    worldOpt.get.addActor(prompt, 624, 470)
    prompt.submitButton.registerClickHandler((Unit) => {
      val move: Move = BuyShares(engine.state.currentPlayer, prompt.selectedCorps)
      engine.makeMove(move)
      worldOpt.get.removeActor(prompt)
      hasSetupHumanMove = false
    })
  }

  private def createBuySharesPrompt: CorpPrompt = {
    val message = "Choose shares to purchase"
    val corpChoices = engine.state.config.corps collect {
      case corp if engine.state.sheet.hasChain(corp) => corp
    }
    val canCheckoutCorp: ((Int, Map[Int, Int]) => Boolean) = (corp, currentSharesMap) => {
      val totalShares = currentSharesMap.values.sum
      if (totalShares >= 3) false
      else {
        val updatedSharesMap: Map[Int, Int] = currentSharesMap.updated(corp,
          if (currentSharesMap.contains(corp)) currentSharesMap(corp) + 1 else 1)
        val correspondingMove: Move = BuyShares(engine.state.currentPlayer, updatedSharesMap)
        engine.state.isLegalMove(correspondingMove)
      }
    }
    val queueLike = false
    new CorpPrompt(engine, message, corpChoices, canCheckoutCorp, queueLike)
  }

  private def createFoundCorpPrompt: CorpPrompt = {
    val message = "Choose a corp to form"
    val corpChoices = engine.state.config.corps collect {
      case corp if !engine.state.sheet.hasChain(corp) => corp
    }
    val canCheckoutCorp: ((Int, Map[Int, Int]) => Boolean) = (corp, currentSharesMap) => {
      currentSharesMap.keySet.isEmpty
    }
    val queueLike = true
    new CorpPrompt(engine, message, corpChoices, canCheckoutCorp, queueLike)
  }

  private def createMergeSurvivorPrompt: CorpPrompt = {
    val message = "Choose the corp that will survive the merger"
    val corpChoices = engine.state.n1CorpsForMerge.get
    val canCheckoutCorp: ((Int, Map[Int, Int]) => Boolean) = (corp, currentSharesMap) => {
      currentSharesMap.keySet.isEmpty
    }
    val queueLike = true
    new CorpPrompt(engine, message, corpChoices, canCheckoutCorp, queueLike)
  }

  private def createMergeEatingPrompt(predatorCorp: Int): CorpPrompt = {
    val message = "Choose the corp that will be merged in\n(first, if there are multiple)"
    val corpChoices =
      for {
        corp <- engine.state.n1CorpsForMerge.get ++ (if (engine.state.n2CorpsForMerge.isDefined) engine.state.n2CorpsForMerge.get else Seq())
        if corp != predatorCorp
      } yield corp
    val canCheckoutCorp: ((Int, Map[Int, Int]) => Boolean) = (corp, currentSharesMap) => {
      currentSharesMap.keySet.isEmpty
    }
    val queueLike = true
    new CorpPrompt(engine, message, corpChoices, canCheckoutCorp, queueLike)
  }

  private def setupHumanMergeCorp1() = {
    val prompt: CorpPrompt = createMergeSurvivorPrompt
    worldOpt.get.addActor(prompt, 624, 470)
    prompt.submitButton.registerClickHandler((Unit) => {
      val selectedCorps = prompt.selectedCorps
      if (selectedCorps.nonEmpty) {
        assert(selectedCorps.size == 1)
        worldOpt.get.removeActor(prompt)
        setupHumanMergeCorp2(selectedCorps.keySet.head)
      }
    })
  }

  private def setupHumanMergeCorp2(predator: Int) = {
    val prompt: CorpPrompt = createMergeEatingPrompt(predator)
    worldOpt.get.addActor(prompt, 624, 470)
    prompt.submitButton.registerClickHandler((Unit) => {
      val selectedCorps = prompt.selectedCorps
      if (selectedCorps.nonEmpty) {
        assert(selectedCorps.size == 1)
        val move = MergeCorp(engine.state.currentPlayer, selectedCorps.keySet.head, predator)
        engine.makeMove(move)
        worldOpt.get.removeActor(prompt)
        hasSetupHumanMove = false
      }
    })
  }

  private def setupHumanMergeTransaction() = {
    val prompt: MergeTransactionPrompt = new MergeTransactionPrompt(engine)
    worldOpt.get.addActor(prompt, 624, 470)
    prompt.submitButton.registerClickHandler((Unit) => {
      val amounts = prompt.getAmounts
      val move = MergeTransaction(engine.state.currentPlayer,
        engine.state.preyCorp.get, engine.state.predatorCorp.get, amounts._2, amounts._3)
      engine.makeMove(move)
      worldOpt.get.removeActor(prompt)
      hasSetupHumanMove = false
    })
  }

  private def setupHumanFoundCorp() = {
    val prompt: CorpPrompt = createFoundCorpPrompt
    worldOpt.get.addActor(prompt, 624, 470)
    prompt.submitButton.registerClickHandler((Unit) => {
      val selectedCorps = prompt.selectedCorps
      if (selectedCorps.nonEmpty) {
        assert(selectedCorps.size == 1)
        val move = FoundCorp(engine.state.currentPlayer, selectedCorps.keySet.head)
        engine.makeMove(move)
        worldOpt.get.removeActor(prompt)
        hasSetupHumanMove = false
      }
    })
  }

  private def setupHumanMove(): Unit = {
    require(!hasSetupHumanMove)
    hasSetupHumanMove = true

    engine.state.expectedMoveType match {
      case MoveType.EndTurnT => aiMove(); hasSetupHumanMove = false
      case MoveType.PlaceTileT => setupHumanPlaceTile()
      case MoveType.FoundCorpT => setupHumanFoundCorp()
      case MoveType.MergeCorpT => setupHumanMergeCorp1()
      case MoveType.MergeTransactionT => setupHumanMergeTransaction()
      case MoveType.BuySharesT => setupHumanBuyShares()
    }
  }

  private def aiMove(): Unit = {
    // ai's make moves at 1 per second
    if (count % 30 == 0) {
      engine.makeMove(engine.state.randomMove.get)
      println(engine.state.prettyPrint)
    }
    count += 1
  }

}
