package acquire.state

/**
  * Immutable game configuration
  * @param _players Sequence of player names in turn order
  * @param _corps Sequence of (corpName, basePrice) in display order
  */
class Config(_players: Seq[String], _corps: Seq[(String, Int)]) {
  private val playersMap: Map[Int, String] = (_players.indices zip _players).toMap
  private val corpsMap: Map[Int, (String, Int)] = (_corps.indices zip _corps).toMap

  /* list of id's */
  val players: Seq[Int] = _players.indices
  val corps: Seq[Int] = _corps.indices

  val numPlayers = players.length
  val numCorps = corps.length

  /* access corp/player names by id */
  def playerName(playerId: Int) = playersMap(playerId)
  def corpName(corpId: Int) = corpsMap(corpId)._1

  /* share price and shareholder bonuses */
  def sharePrice(corpId: Int)(chainSize: Option[Int]): Option[Int] =
    sharePriceHelper(corpsMap(corpId)._2, chainSize.flatMap(priceLevel))
  def firstBonus(corpId: Int)(chainSize: Option[Int]): Option[Int] =
    sharePrice(corpId)(chainSize).map(_*10)
  def secondBonus(corpId: Int)(chainSize: Option[Int]): Option[Int] =
    sharePrice(corpId)(chainSize).map(_*5)

  /* share price is a function of the corp's base price and current price level */
  private def sharePriceHelper(basePrice: Int, priceLevel: Option[Int]): Option[Int] =
    priceLevel.map(pl => basePrice + 100 * (pl - 2))

  /* the price level determines the share price */
  private def priceLevel(chainSize: Int): Option[Int] = chainSize match {
    case n if n  <  2 => None
    case n if n <=  5 => Some(n)
    case n if n <= 40 => Some((n-1)/10 + 6)
    case n if n >= 41 => Some(10)
  }
}
