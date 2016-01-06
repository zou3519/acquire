package acquire.state

import acquire.state.MoveType.MoveType
import acquire.state.Shareholder.Shareholder

import scala.collection.mutable
import acquire.state.impl.{BoardImpl, ScoreSheetImpl}
import mcts.State

import scala.util.Random

class AcquireState private(val config: Config, _tileRack: Vector[mutable.HashSet[Location]],
                           val board: Board, val sheet: ScoreSheet) extends State[Move] {
  val numPlayers: Int = config.players.length
  val numCorps: Int = config.corps.length

  private var _isOver: Boolean = false               // if the game is over
  private var _whoseTurn: Int = 0                    // which player has a turn
  private var _currentPlayer: Int = 0                // which player is making a move (inside someone's turn)
  private var _expectedMoveType = MoveType.PlaceTileT// the move type the state is expecting.
  private var _tilePlaced: Option[Location] = None   // the tile that was placed this turn
  private var _mergerOccurring = false               // if a merger is occurring
  private var _predatorCorp: Option[Int] = None      // the corporation that will survive the merger (if merger)
  private var _preyCorp: Option[Int] = None          // the corporation that will be eaten by the merger (if merger)
  private var _n1CorpsForMerge: Option[Seq[Int]] = None // when merging, the corps around the tile that are the largest
  private var _n2CorpsForMerge: Option[Seq[Int]] = None // when merging, the next largest corps (sometimes not needed)

  def whoseTurn        = _whoseTurn
  def currentPlayer    = _currentPlayer
  def expectedMoveType = _expectedMoveType
  def tilePlaced       = _tilePlaced
  def mergerOccurring  = _mergerOccurring
  def predatorCorp     = _predatorCorp
  def preyCorp         = _preyCorp

  def this(config: Config) = {
    this(config, Vector.fill(config.players.length)(new mutable.HashSet), new BoardImpl(), new ScoreSheetImpl(config))
    for (player <- config.players; i <- 0 until 6) {
      _tileRack(player) += board.tiles.dequeue()
    }
  }

  def tileRack(player: Int): mutable.HashSet[Location] = _tileRack(player)

  override def isOver: Boolean = _isOver

  /* after the game has ended, outcome is a vector of the player's scores */
  var outcome: Option[IndexedSeq[Double]] = None

  private def legalEndTurnMoves: IndexedSeq[EndTurn] =
    if (canEndGame) Vector(false, true).map(b => EndTurn(currentPlayer, b))
    else Vector(EndTurn(currentPlayer, endGame = false))

  // there can be a lot of these
  private def legalBuySharesMoves: Iterator[BuyShares] = {
    val shares: Seq[Option[Int]] = config.corps.filter(sheet.hasChain) flatMap {
      corp => {
        val maxPurchase: Int = math.min(3, sheet.cash(currentPlayer) / sheet.sharePrice(corp).get)
        Vector().padTo(math.min(maxPurchase, sheet.chainSize(corp).get), Some(corp))
      }
    }
    val sharesWithNones: Seq[Option[Int]] = shares ++ Seq(None, None, None)
    val combos: Iterator[Seq[Option[Int]]] = sharesWithNones.combinations(3)
    val allShareMaps: Iterator[Map[Int, Int]] = combos.map(combo => combo.groupBy(identity) collect {
      case (Some(num), lst) => (num, lst.size)
    })
    allShareMaps collect {
      case shareMap if isValidBuySharesMap(shareMap) => BuyShares(currentPlayer, shareMap)
    }
  }

  private def legalFoundCorpMoves: IndexedSeq[FoundCorp] =
    config.corps collect {
      case corp if !sheet.hasChain(corp) => FoundCorp(currentPlayer, corp)
    } toIndexedSeq

  private def legalPlaceTileMoves: IndexedSeq[PlaceTile] =
    tileRack(currentPlayer).toIndexedSeq.filter(canPlaceTile).map(tile => PlaceTile(currentPlayer, tile))

  private def legalMergeCorpMoves: IndexedSeq[MergeCorp] = {
    val sizeOfN1Corps = _n1CorpsForMerge.map(_.size)
    val sizeOfN2Corps = _n2CorpsForMerge.map(_.size)

    (sizeOfN1Corps, sizeOfN2Corps) match {
      case (Some(1), Some(1)) =>
        Vector(MergeCorp(currentPlayer, _n2CorpsForMerge.get.head,  _n1CorpsForMerge.get.head))

      case (Some(1), Some(n)) =>
        _n2CorpsForMerge.get flatMap {
          corp => Vector(MergeCorp(currentPlayer,corp, _n1CorpsForMerge.get.head))
        } toIndexedSeq

      case (Some(n), None) =>
        _n1CorpsForMerge.get.combinations(2).flatMap(_.permutations) map {
          pair => MergeCorp(currentPlayer, pair.head, pair.last)
        } toIndexedSeq

      case _ => assert(assertion = false, "should never get here"); Vector()
    }
  }

  private def legalMergeTransactionMoves: IndexedSeq[MergeTransaction] = {
    val total = sheet.shares(preyCorp.get, currentPlayer)
    val tradeLimit = sheet.bankShares(predatorCorp.get)*2
    for {
      trade <- 0 to tradeLimit by 2
      sell <- 0 to (total - trade)
    } yield MergeTransaction(currentPlayer, preyCorp.get, predatorCorp.get, sell, trade)
  }


  override def legalMoves: IndexedSeq[Move] = {
    if (isOver) Vector() else
    _expectedMoveType match {
      case MoveType.EndTurnT => legalEndTurnMoves
      case MoveType.BuySharesT => legalBuySharesMoves toIndexedSeq
      case MoveType.FoundCorpT => legalFoundCorpMoves
      case MoveType.PlaceTileT => legalPlaceTileMoves
      case MoveType.MergeCorpT => legalMergeCorpMoves
      case MoveType.MergeTransactionT => legalMergeTransactionMoves
    }
  }

  def isLegalMove(move: Move): Boolean = {
    if (MoveType.typeOf(move) != expectedMoveType) return false
    if (move.player != currentPlayer) return false

    move match {
      case EndTurn(player, endTurn) => ???
      case BuyShares(player, shareMap) => isValidBuyShares(move.asInstanceOf[BuyShares])
      case FoundCorp(player, corp) => !sheet.hasChain(corp)
      case PlaceTile(player, tile) => tileRack(player).contains(tile) && canPlaceTile(tile)
      case MergeCorp(player, prey, predator) => ???
      case MergeTransaction(player, prey, predator, sellAmt, buyAmt) =>  ???
    }
  }

  private def isValidBuyShares(move: BuyShares): Boolean = {
    var totalPrice: Int = 0
    for ((id, num) <- move.corpToNum) {
      if (sheet.bankShares(id) < num || !sheet.hasChain(id)) return false
      totalPrice += sheet.sharePrice(id).get * num
    }
    totalPrice <= sheet.cash(move.player)
  }

  private def isValidBuySharesMap(sharesMap: Map[Int, Int]): Boolean = {
    var totalPrice: Int = 0
    for ((id, num) <- sharesMap) {
      if (sheet.bankShares(id) < num || !sheet.hasChain(id)) return false
      totalPrice += sheet.sharePrice(id).get * num
    }
    totalPrice <= sheet.cash(currentPlayer)
  }

  override def randomMove: Option[Move] = {
    val moves = legalMoves
    val choice = Random.nextInt(moves.length)
    if (moves.isEmpty) None else Some(moves(choice))
  }

  /* next state does not mutate but moveInPlace does */
  override def nextState(move: Move): State[Move] = {
    val next = copy
    next.moveInPlace(move)
    next
  }

  override def moveInPlace(move: Move): Unit = {
    require(_expectedMoveType == MoveType.typeOf(move), "has expected move type")
    require(move.player == _currentPlayer, "correct player is playing")
    move match {
      case EndTurn(player, endGame) => endTurn(player, endGame)
      case BuyShares(player, shareMap) => buyShares(player, shareMap)
      case FoundCorp(player, corp) => foundCompany(player, corp)
      case PlaceTile(player, tile) => placeTile(player, tile)
      case MergeCorp(player, prey, predator) => mergeCorp(player, prey, predator)
      case MergeTransaction(player, prey, predator, sellAmt, buyAmt) => mergeTransaction(player, sellAmt, buyAmt)
    }
  }

  override def copy: AcquireState = {
    val newState = new AcquireState(config, _tileRack.map(_.clone), board.copy, sheet.copy)
    newState._whoseTurn        = _whoseTurn
    newState._currentPlayer    = _currentPlayer
    newState._expectedMoveType = _expectedMoveType
    newState._tilePlaced       = _tilePlaced
    newState._mergerOccurring  = _mergerOccurring
    if (_mergerOccurring) {
      newState._predatorCorp     = _predatorCorp
      newState._preyCorp         = _preyCorp
      newState._n1CorpsForMerge  = _n1CorpsForMerge
      newState._n2CorpsForMerge  = _n2CorpsForMerge
    }
    newState
  }


  /*
    ================================
    PRIVATE METHODS FOR MAKING MOVES
    ================================
   */

  def canEndGame: Boolean =
    config.corps.exists(sheet.chainSize(_).getOrElse(0) >= 41) ||
      (config.corps.forall(sheet.chainSize(_).getOrElse(11) >= 11) && config.corps.exists(sheet.hasChain))

  private def endTurn(player: Int, endGame: Boolean): Unit = {
    require(!endGame || canEndGame, "end game condition met")

    if (endGame) {
      _isOver = true
      for (corp <- config.corps.filter(sheet.hasChain)) {
        for ((player, bonus) <- bonuses(corp)) {
          sheet.setCash(player)(sheet.cash(player) + bonus)
        }
      }
      outcome = Some(config.players map {sheet.netWorth(_).toDouble} toIndexedSeq)
      return
    }

    // replace any dead tiles
    _tileRack(player).foreach(tile => if (areNeighborsSafe(tile)) _tileRack(player) -= tile)
    while (tileRack(player).size < 6 && board.tiles.nonEmpty) {
      val nextTile = board.tiles.dequeue()
      _tileRack(player) += nextTile
    }

    beginTurn(nextPlayer(player))
  }


  private def proceedToDecision(player: Int, desiredMoveType: MoveType): Unit = {
    this._currentPlayer = player
    this._expectedMoveType = desiredMoveType

    desiredMoveType match {
      case MoveType.MergeCorpT => beginMerger()
      case _ => ()
    }
  }

  private def placeTile(player: Int, tile: Location): Unit = {
    require(_currentPlayer == _whoseTurn, "players can only place a tile when it's their turn")
    require(canPlaceTile(tile), "valid tile to place")

    board.setTileAt(tile)(OrphanTile())
    _tileRack(player) -= tile
    _tilePlaced = Some(tile)

    // strategy: figure out the unique corps, and if we have any orphans.
    val neighborTileTypes = tile.neighbors.map(board.tileAt).distinct.filter(!_.isInstanceOf[EmptyTile])
    val (uniqueCorps, uniqueOrphans) = neighborTileTypes.partition(_.isInstanceOf[CorpTile])

    if (uniqueCorps.nonEmpty) {
      uniqueCorps.size match {
        case 1 => // expand corp size
          val corpTile = uniqueCorps.head.asInstanceOf[CorpTile]
          val modified: Int = fillNeighborsAndMe(tile, corpTile)
          sheet.setChainSize(corpTile.corpId)(sheet.chainSize(corpTile.corpId).map(_ + modified))
          proceedToDecision(player, MoveType.BuySharesT)
          return

        case _ => // merge corps
          proceedToDecision(player, MoveType.MergeCorpT)
          return
      }
    } else if (uniqueOrphans.nonEmpty) {
      proceedToDecision(player, MoveType.FoundCorpT)
      return
    }

    proceedToDecision(player, MoveType.BuySharesT)
  }

  private def foundCompany(player: Int, corp: Int): Unit = {
    require(!sheet.hasChain(corp))

    val modified: Int = fillNeighborsAndMe(_tilePlaced.get, CorpTile(corp))
    sheet.setChainSize(corp)(Some(sheet.chainSize(corp).getOrElse(0) + modified))

    if (sheet.bankShares(corp) > 0) sheet.giftShare(player, corp)
    proceedToDecision(player, MoveType.BuySharesT)
  }

  /* maps for Majority and Minority shareholders only */
  private def topShareholders(corp: Int): Map[Shareholder, Seq[Int]] = {
    val playersWithShares = config.players collect {
      case player if sheet.shares(corp, player) > 0 => (player, sheet.shares(corp, player))
    }
    if (playersWithShares.isEmpty)
      return Map(Shareholder.Majority -> List(), Shareholder.Minority -> List())

    val n1Shares = playersWithShares.maxBy(_._2)._2
    val (playersWithN1Shares, otherPlayers) = playersWithShares.partition(_._2 == n1Shares)
    if (playersWithN1Shares.length > 1 || otherPlayers.isEmpty)
      return Map(
        Shareholder.Majority -> playersWithN1Shares.map(_._1),
        Shareholder.Minority -> List()
      )

    val n2Shares = otherPlayers.maxBy(_._2)._2
    val playersWithN2Shares = otherPlayers.filter(_._2 == n2Shares)
      Map(Shareholder.Majority -> playersWithN1Shares.map(_._1),
          Shareholder.Minority -> playersWithN2Shares.map(_._1))
  }

  // map players to their bonuses
  private def bonuses(corp: Int): Map[Int, Int] = {
    require(sheet.hasChain(corp), "corp must be formed")

    val holderInfo: Map[Shareholder, Seq[Int]] = topShareholders(corp)
    val majorityHolders: Seq[Int] = holderInfo(Shareholder.Majority)
    val minorityHolders: Seq[Int] = holderInfo(Shareholder.Minority)
    (majorityHolders.length, minorityHolders.length) match {
      case (0, 0) => Map()
      case (n, 0) =>
        val payout: Int = (sheet.firstBonus(corp).get + sheet.secondBonus(corp).get)/n
        majorityHolders map {p => (p, payout)} toMap
      case (1, m) =>
        val minorityPayout: Int = sheet.secondBonus(corp).get / m
        val majMap: Map[Int, Int] = majorityHolders map {p => (p, sheet.firstBonus(corp).get)} toMap
        val minMap: Map[Int, Int] = minorityHolders map {p => (p, minorityPayout)} toMap;
        majMap ++ minMap
      case _ => assert(assertion = false, "can never get here"); Map()
    }
  }

  private def mergeCorp(player: Int, prey: Int, predator: Int): Unit = {
    require(_mergerOccurring, "merger must be occurring")
    require(_n1CorpsForMerge.get.contains(predator), "predator must be one of the largest corps")
    require(_n1CorpsForMerge.get.contains(prey) || _n2CorpsForMerge.get.contains(prey),
      "prey has to be one of the second largest corps")

    for ((player, bonus) <- bonuses(prey)) {
      sheet.setCash(player)(sheet.cash(player) + bonus)
    }



    makeMergeChoice(prey, predator)
    proceedToDecision(player, MoveType.MergeTransactionT)
  }

  private def beginMerger(): Unit = {
    require(!_mergerOccurring, "merger must not be occurring")
    require(!areNeighborsSafe(tilePlaced.get))
    _mergerOccurring = true

    val neighborCorps = tilePlaced.get.neighbors.map(board.tileAt) collect { case CorpTile(id) => id } distinct
    val groupedCorps = neighborCorps.groupBy(corp => sheet.chainSize(corp).get)
    val sortedSizes = groupedCorps.keys.toSeq.sorted.reverse.take(2)
    sortedSizes.length match {
      case 1 => _n1CorpsForMerge = Some(neighborCorps)
      case 2 =>
        val n1 = sortedSizes.head
        val n2 = sortedSizes(1)
        _n1CorpsForMerge = Some(groupedCorps(n1))
        if (groupedCorps(n1).length == 1) {
          _n2CorpsForMerge = Some(groupedCorps(n2))
        }
    }
  }

  private def makeMergeChoice(preyCorp: Int, predatorCorp: Int): Unit = {
    _preyCorp = Some(preyCorp)
    _predatorCorp = Some(predatorCorp)
  }

  private def endMerger(): Unit = {
    require(_mergerOccurring, "merger must be occurring")

    // perform the honor of modifying the grid
    val tile = _tilePlaced.get
    fillNeighborsAndMe(tile, CorpTile(preyCorp.get))
    val modified = paintBucket(tile, CorpTile(predatorCorp.get))
    sheet.setChainSize(preyCorp.get)(None)
    sheet.setChainSize(predatorCorp.get)(sheet.chainSize(predatorCorp.get).map(_ + modified))

    _mergerOccurring = false
    _preyCorp = None
    _predatorCorp = None
    _n1CorpsForMerge = None
    _n2CorpsForMerge = None
  }

  private def nextPlayer(player: Int): Int = (player + 1) % numPlayers

  private def mergeTransaction(player: Int, sellAmt: Int, tradeAmt: Int): Unit = {
    require(_mergerOccurring, "merger must be occurring")
    require(tradeAmt % 2 == 0, "must trade even")
    require(sheet.shares(preyCorp.get, player) >= tradeAmt + sellAmt, "enough prey shares to transfer")
    require(sheet.bankShares(predatorCorp.get) >= tradeAmt/2, "enough predator shares to transfer")

    // checks passed; process transaction
    if (tradeAmt > 0)
      sheet.tradeShares(player, preyCorp.get, predatorCorp.get, tradeAmt)
    if (sellAmt > 0)
      sheet.sellShares(player, preyCorp.get, sellAmt)

    // if we need to keep on processing these transactions...
    if (nextPlayer(player) != whoseTurn) {
      proceedToDecision(nextPlayer(player), MoveType.MergeTransactionT)
      return
    }

    endMerger()

    // otherwise,
    // see if we need to merge any more
    // strategy: figure out the unique corps, and if we have any orphans.
    val tile = _tilePlaced.get
    val neighborTileTypes = tile.neighbors.map(board.tileAt).distinct.filter(!_.isInstanceOf[EmptyTile])
    val uniqueCorps = neighborTileTypes.filter(_.isInstanceOf[CorpTile])

    uniqueCorps.size match {
      case 1 => // no need to merge anymore
        proceedToDecision(nextPlayer(player), MoveType.BuySharesT)

      case _ => // more merging happens
        proceedToDecision(nextPlayer(player), MoveType.MergeCorpT)
    }
  }


  private def buyShares(player: Int, bought: Map[Int, Int]): Unit = {
    // check that the player can afford the shares
    var totalPrice: Int = 0
    for ((id, num) <- bought) {
      require(sheet.bankShares(id) >= num && sheet.hasChain(id), "enough shares to purchase and valid shares")
      totalPrice += sheet.sharePrice(id).get * num
    }
    require(totalPrice <= sheet.cash(player), "player has enough cash")

    // buy the shares
    for ((id, num) <- bought) sheet.buyShares(player, id, num)

    proceedToDecision(player, MoveType.EndTurnT)
  }

  private def beginTurn(player: Int): Unit = {
    require(nextPlayer(_whoseTurn) == player, "enforce player ordering")
    _whoseTurn = player
    _tilePlaced = None
    if (tileRack(player).forall(tile => !canPlaceTile(tile))) {
      proceedToDecision(player, MoveType.BuySharesT)
    } else {
      proceedToDecision(player, MoveType.PlaceTileT)
    }
  }


  /**
    * There are 2 situations where we cannot place a tile:
    * 1) It would merge 2 corps that are already safe
    * 2) It would found a new corp but all corps are founded
    * @param tile The tile
    * @return If we can place the tile
    */
  private def canPlaceTile(tile: Location): Boolean = {
    noTileAtLocation(tile) && !areNeighborsSafe(tile) && !(allCorpsFormed && !willNotFormNewCorp(tile))
  }

  private def willNotFormNewCorp(tile: Location) = noCorpsFormed || hasCorpNeighbor(tile) || !hasOrphanNeighbor(tile)
  private def noCorpsFormed = !config.corps.exists(sheet.hasChain)
  private def allCorpsFormed = config.corps.forall(sheet.hasChain)

  private def noTileAtLocation(loc: Location) = board.tileAt(loc).isInstanceOf[EmptyTile]
  private def hasOrphanNeighbor(tile: Location) = neighborTiles(tile).exists(_.isInstanceOf[OrphanTile])
  private def hasCorpNeighbor(tile: Location) = neighborTiles(tile).exists(_.isInstanceOf[CorpTile])

  private def areNeighborsSafe(tile: Location): Boolean = {
    val neighborCorps = tile.neighbors.map(board.tileAt).distinct.filter(_.isInstanceOf[CorpTile]).map(_.asInstanceOf[CorpTile]).map(_.corpId)
    if (neighborCorps.length < 2) false
    else neighborCorps.forall(isCorpSafe)
  }

  private def isCorpSafe(corp: Int) = sheet.chainSize(corp) match {
    case None => false
    case Some(n) => n >= 11
  }

  private def neighborTiles(tile: Location): List[Tile] = tile.neighbors.map(board.tileAt)

  /**
    * Acts as a paint bucket. Given a collection of adjacent tiles that have the same TileType,
    * make them into the desired TileType
    */
  private def paintBucket(tile: Location, desiredType: Tile): Int = {
    val toCheck: mutable.Queue[Location] = new mutable.Queue()
    var checked: mutable.HashSet[Location] = new mutable.HashSet()
    val prevType: Tile = board.tileAt(tile)
    var modified: Int = 0

    toCheck.enqueue(tile)
    while (toCheck.nonEmpty) {
      val loc: Location = toCheck.dequeue()
      if (board.tileAt(loc) == prevType) {
        board.setTileAt(loc)(desiredType)
        modified += 1
        for (neighbor <- loc.neighbors) {
          if (!checked.contains(neighbor)) {
            toCheck.enqueue(neighbor)
          }
        }
      }
      checked += loc
    }

    modified
  }

  /** Less intensive version of paintBucket. Can only fill in the tile and its 4 neighbors. */
  private def fillNeighborsAndMe(tile: Location, desiredType: Tile): Int = {
    var modified: Int = 1
    val prevType: Tile = board.tileAt(tile)

    board.setTileAt(_tilePlaced.get)(desiredType)
    for (neighbor <- _tilePlaced.get.neighbors) {
      if (board.tileAt(neighbor) == prevType) {
        board.setTileAt(neighbor)(desiredType)
        modified += 1
      }
    }
    modified
  }

  /* ------------------------- PRINTING ------------------------------ */
  private def prettyPrintStats() = {
    val rows = Vector.concat(config.players.map(config.playerName).toVector, Vector("sz", "bank","cost","lg$", "sm$" ))
    val cols = Vector.concat(config.corps.map(config.corpName).map(_.charAt(0).toString).toVector, Vector("$$", "net"))

    def intOptToString(intOpt: Option[Int]) = intOpt.map(_.toString).getOrElse("--")

    /* how to display a row, given a function that reveals some aspect of a corp */
    def rowView(viewFn: (Int => Option[Int])): (Seq[Int] => Seq[String]) =
      (corps: Seq[Int]) => config.corps.map(viewFn).map(intOptToString)

    var fns: Vector[Seq[Int] => Seq[String]] = Vector(
      rowView(sheet.chainSize),
      rowView(corp => Some(sheet.bankShares(corp))),
      rowView(sheet.sharePrice(_).map(_/100)),
      rowView(sheet.sharePrice(_).map(_/10)),
      rowView(sheet.sharePrice(_).map(_/20))
    )

    val moreFns: Vector[Seq[Int] => Seq[String]] =
      config.players.map(player => (corps: Seq[Int]) =>
        Seq.concat(corps.map((id: Int) => sheet.shares(id, player)).map((num: Int) => num.toString), Seq((sheet.cash(player)/100).toString,
          (sheet.netWorth(player)/100).toString))
      ).toVector

    fns = Vector.concat(moreFns, fns)

    val firstRow: String = "     " + cols.map(_.padTo(3, ' ')).mkString(" ")
    val nextRows: Seq[String] = for (i <- fns.indices) yield {
      rows(i).padTo(4, ' ') + " " + fns(i)(config.corps).map(_.padTo(3, ' ')).mkString(" ")
    }
    Seq.concat(Seq(firstRow), nextRows).mkString("\n")
  }

  def prettyPrint = {
    val board = prettyPrintBoard()
    val stats = prettyPrintStats()
    val boardSep = board.split("\n").toVector
    val statsSep = stats.split("\n").toVector
    val line = "".padTo(80, '-')
    val rows = for (i <- boardSep.indices) yield boardSep(i) + "   " + statsSep(i)
    line + "\n" + prettyPrintInfo + "\n" + rows.mkString("\n") + "\n" + prettyPrintTileRack + "\n" + line
  }

  private def prettyPrintTileRack = {
    _tileRack.map(_.toString).mkString(" | ")
  }
  private def prettyPrintInfo = {
    val somelst = List(_whoseTurn, _currentPlayer, _expectedMoveType, _tilePlaced,
      _mergerOccurring, _predatorCorp, _preyCorp, _n1CorpsForMerge, _n2CorpsForMerge)
    somelst.map(_.toString).mkString(", ")
  }

  private def prettyPrintBoard() = {
    def rowView(row: Int): Seq[String] = row match {
      case 9 => for (r <- 0 to board.Cols) yield if (r == 0) "" else r.toString
      case _ =>
        val tiles = for (c <- 0 until board.Cols) yield {
          board.tileAt(Locations.Store(row)(c)) match {
            case EmptyTile() => "-"
            case OrphanTile() => "#"
            case CorpTile(id) => config.corpName(id).charAt(0).toString
          }
        }
        Seq.concat(Seq((row + 65).toChar + ":"), tiles)
    }

    val lines = for (row <- 0 to board.Rows) yield {
      rowView(row).map(_.padTo(2, ' ')).mkString("")
    }
    lines.mkString("\n")
  }
}
