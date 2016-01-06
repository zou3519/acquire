package acquire.state.impl

import acquire.state.{Config, ScoreSheet}

class ScoreSheetImpl private(config: Config, _shares: Array[Array[Int]], _cash: Array[Int],
                             _bankShares: Array[Int], _chainSize: Array[Option[Int]]) extends ScoreSheet {
  def this(config: Config) =
    this(config,
      Array.fill(config.numCorps, config.numPlayers)(0),
      Array.fill(config.numPlayers)(6000),
      Array.fill(config.numCorps)(25),
      Array.fill[Option[Int]](config.numCorps)(None))

  override def cash(player: Int) = _cash(player)
  override def setCash(player: Int)(amt: Int): Unit = _cash(player) = amt
  override def netWorth(player: Int): Int = {
    val shareAmts = for (corp <- config.corps) yield (corp, _shares(corp)(player))
    val totalSharePrice = (for ((corp, amt) <- shareAmts) yield sharePrice(corp).getOrElse(0)*amt).sum
    totalSharePrice + _cash(player)
  }

  override def shares(player: Int, amt: Int) = _shares(player)(amt)
  override def setShares(corp: Int, player: Int)(num: Int): Unit = {
    require(num >= 0 && num < 25)
    _shares(corp)(player) = num
  }
  override def bankShares(corp: Int) = _bankShares(corp)
  override def setBankShares(corp: Int)(num: Int): Unit = {
    require(num >= 0 && num < 25)
    _bankShares(corp) = num
  }

  override def giftShare(player: Int, corp: Int): Unit = {
    require(_bankShares(corp) > 0)

    _shares(corp)(player) += 1
    _bankShares(corp) -= 1
  }
  override def buyShares(player: Int, corp: Int, amt: Int): Unit = {
    require(hasChain(corp), "corp must have a chain")
    require(amt <= _bankShares(corp), "enough shares left in corp")
    val price = sharePrice(corp).get * amt
    require(price <= _cash(player), "enough cash for purchase")

    _bankShares(corp) -= amt
    _shares(corp)(player) += amt
    _cash(player) -= price
  }
  override def sellShares(player: Int, corp: Int, amt: Int): Unit = {
    require(_shares(corp)(player) >= amt, "enough shares to sell")
    require(hasChain(corp), "corp must have a chain")

    _bankShares(corp) += amt
    _shares(corp)(player) -= amt
    _cash(player) += amt * sharePrice(corp).get
  }
  override def tradeShares(player: Int, prey: Int, predator: Int, amt: Int): Unit = {
    require(amt % 2 == 0, "trading even amount")
    require(_shares(prey)(player) >= amt, "enough shares to trade")
    require(_bankShares(predator) >= amt/2, "enough shares to receive")

    _shares(prey)(player) -= amt
    _bankShares(prey) += amt

    _shares(predator)(player) += amt/2
    _bankShares(predator) -= amt/2
  }

  /* corp methods */
  override def chainSize(corp: Int): Option[Int] = _chainSize(corp)
  override def setChainSize(corp: Int)(num: Option[Int]): Unit = {
    require(num.isEmpty || num.get >= 2)
    _chainSize(corp) = num
  }

  override def unformedCorps: Seq[Int] = config.corps.filter(_chainSize(_).isEmpty)
  override def sharePrice(corp: Int): Option[Int] = config.sharePrice(corp)(_chainSize(corp))

  /* deep copy */
  override def copy: ScoreSheet =
    new ScoreSheetImpl(config, _shares.map(_.clone), _cash.clone(), _bankShares.clone(), _chainSize.clone())
}
