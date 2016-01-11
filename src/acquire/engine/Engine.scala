package acquire.engine

import acquire.engine.PlayerType.PlayerType
import acquire.state._

object EngineDefaults {
  val Corps = Seq("Tower", "Luxor", "American", "Worldwide", "Festival", "Imperial", "Continental").zip(
    Seq(200, 200, 300, 300, 300, 400, 400))

  // TODO: break into functions
  def describeMoveRecord(moveRecord: MoveRecord): String = moveRecord match {
    case MoveRecord(state, move) => {
      val player: String = state.config.playerName(move.playerId)
      move match {
        case EndTurn(playerId, endGame) =>
          val nextPlayer = state.config.playerName((playerId + 1)%state.numPlayers)
          if (endGame) {
            val corpBonusMessages: Seq[Seq[String]] = for {
              corpId <- state.config.corps
              if state.sheet.hasChain(corpId)
            } yield {
              val topShareholders = state.topShareholders(corpId)
              val bonuses = state.bonuses(corpId)
              val corp = state.config.corpName(corpId)
              val bonusMessages: Seq[String] =
                (for ((holdingGroup, holders) <- topShareholders; holderId <- holders) yield {
                  val holder = state.config.playerName(holderId)
                  val bonus = bonuses(holderId)
                  f"$holder%s ($holdingGroup%s holder of $corp%s) received $$$bonus%d"
                }).toSeq
              bonusMessages
            }

            f"$player%s ended the game.\n" + corpBonusMessages.flatten.mkString("\n")
          }
          else f"--------------- $nextPlayer%s's turn ---------------"

        case PlaceTile(_, location) =>
          f"$player%s placed tile $location%s"

        case FoundCorp(_, corpId) =>
          val corp: String = state.config.corpName(corpId)
          val giftShareMessage: String = if (state.sheet.bankShares(corpId) > 0) " and received 1 share." else ""
          f"$player%s founded $corp%s$giftShareMessage%s"

        case MergeCorp(_, preyCorpId, predatorCorpId) =>
          val preyCorp = state.config.corpName(preyCorpId)
          val predatorCorp = state.config.corpName(predatorCorpId)
          val topShareholders = state.topShareholders(preyCorpId)
          val bonuses = state.bonuses(preyCorpId)
          val mergeMessages: Seq[String] =
            (for ((holdingGroup, holders) <- topShareholders; holderId <- holders) yield {
              val holder = state.config.playerName(holderId)
              val bonus = bonuses(holderId)
              f"$holder%s ($holdingGroup%s) received $$$bonus%d"
            }).toSeq

          f"$player%s is merging $preyCorp%s into $predatorCorp%s\n" +
            f"Shareholder bonuses for $preyCorp will be paid out.\n" +
            mergeMessages.mkString("\n")

        case MergeTransaction(_, preyCorpId, predatorCorpId, sellAmt, tradeAmt) =>
          val prey = state.config.corpName(preyCorpId)
          val predator = state.config.corpName(predatorCorpId)
          val newShares = tradeAmt/2
          val keptAmt = state.sheet.shares(preyCorpId, state.currentPlayer) - sellAmt - tradeAmt
          f"$player%s kept $keptAmt%d, sold $sellAmt%d, and traded $tradeAmt%d share(s) of $prey%s. " +
            f"Received $newShares%d share(s) of $predator%s"

        case BuyShares(_, sharesMap) =>
          if (sharesMap.isEmpty) {
            if (state.config.corps.forall(corp => !state.sheet.hasChain(corp) || state.sheet.chainSize(corp).get == 0)) {
              f"$player%s could not buy shares: there were no shares available for purchase"
            } else if (state.legalMoves.length == 1) {
              f"$player%s could not afford any shares"
            } else {
              f"$player%s bought no shares"
            }
          }
          else f"$player%s bought " + sharesMap.map {
            case (corpId, amt) =>
              val corp = state.config.corpName(corpId)
              f"$amt%d share(s) of $corp"
          }.mkString(" and ")
      }
    }
  }
}

class Engine(players: IndexedSeq[(String, PlayerType)]) {
  val config: Config = new Config(players.map(_._1), EngineDefaults.Corps)
  private var _state: AcquireState = new AcquireState(config)

  private var _numMoves: Int = 0                              // # moves people have made
  private var _history: IndexedSeq[TurnRecord] = Vector()     // list of (state, move chosen from this state)
  private var _currentTurnRecord: TurnRecord = new TurnRecord // the current turn record
  private var _corpHQ: Map[Int, Option[Location]] =           // map of corp to corp headquarters
    (0 until 7).map(i => (i, None)) toMap

  def state: AcquireState = _state
  def history: IndexedSeq[TurnRecord] = _history
  def corpHQ: Map[Int, Option[Location]] = _corpHQ
  def currentTurnRecord: TurnRecord = _currentTurnRecord
  def numMoves: Int = _numMoves
  def flatHistory: IndexedSeq[MoveRecord] = _history.flatMap(_.records) ++ _currentTurnRecord.records
  def currentPlayerType = players(state.currentPlayer)._2

  def makeMove(move: Move): Unit = {
    // TODO: check to see if the move is legal ?
    println("Attempting to move: " + EngineDefaults.describeMoveRecord(MoveRecord(state, move)))
    val nextState = _state.nextState(move)

    // update history
    _currentTurnRecord = _currentTurnRecord.addRecord(new MoveRecord(_state, move))
    if (_state.expectedMoveType == MoveType.EndTurnT) {
      _history = _history :+ _currentTurnRecord
      _currentTurnRecord = new TurnRecord
    }

    // keep track of corpHQ metadata
    move match {
      case FoundCorp(_,corp) => _corpHQ = _corpHQ.updated(corp, Some(_state.tilePlaced.get))
      case MergeCorp(_,prey,_) => _corpHQ = _corpHQ.updated(prey, None)
      case _ => ()
    }

    // prepare for the next move
    _state = nextState
    _numMoves += 1

    println(_state.prettyPrint)

    // if there's only one option make the option
    val legalMoves = state.legalMoves
    if (legalMoves.length == 1 && state.expectedMoveType != MoveType.PlaceTileT) {
      makeMove(legalMoves.head)
    }
  }
}
