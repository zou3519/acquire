package acquire.state

/**
  * The scoresheet keeps track of player and corp information.
  */
trait ScoreSheet {
  /* get/set player cash */
  def cash(player: Int): Int
  def setCash(player: Int)(cash: Int): Unit
  def netWorth(player: Int): Int

  /* player share transactions */
  def giftShare(player: Int, corp: Int): Unit
  def buyShares(player: Int, corp: Int, amt: Int): Unit
  def sellShares(player: Int, corp: Int, amt: Int): Unit
  def tradeShares(player: Int, prey: Int, predator: Int, amt: Int): Unit

  /* get/set corp shares */
  def shares(corp: Int, player: Int): Int
  def setShares(corp: Int, player: Int)(num: Int): Unit
  def bankShares(corp: Int): Int
  def setBankShares(corp: Int)(num: Int): Unit

  /* pricing and bonuses */
  def sharePrice(corp: Int): Option[Int]
  def firstBonus(corp: Int): Option[Int] = sharePrice(corp).map(_*10)
  def secondBonus(corp: Int): Option[Int] = sharePrice(corp).map(_*5)

  /* corp methods */
  def chainSize(corp: Int): Option[Int]
  def setChainSize(corp: Int)(num: Option[Int]): Unit
  def isSafe(corp: Int): Boolean = chainSize(corp).exists(_ >= 11)
  def hasChain(corp: Int): Boolean = chainSize(corp).nonEmpty
  def unformedCorps: Seq[Int]

  /* deep copy */
  def copy: ScoreSheet
}
