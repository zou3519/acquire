package acquire.gui

import acquire.engine.Engine
import acquire.state.{Move, BuyShares}

import scala.collection.mutable.ArrayBuffer

//  engine.state.config.playerName(engine.state.currentPlayer) + // keep this here
class BuySharesPrompt(engine: Engine) extends CorpPrompt(engine, "Choose shares to buy") {

  override val corpButtons: ArrayBuffer[CorpButton] = {
    val viableCorps = for {
      corp <- engine.state.config.corps
      if engine.state.sheet.hasChain(corp) && engine.state.sheet.bankShares(corp) > 0 &&
        engine.state.sheet.sharePrice(corp).get < engine.state.sheet.cash(engine.state.currentPlayer)
    } yield new CorpButton(engine, corp)
    for (button <- viableCorps) {
      button.registerClickHandler((Unit) => addCheckoutCorpButton(button.corp))
    }
    viableCorps.to[ArrayBuffer]
  }

  override def addCheckoutCorpButton(corp: Int): Unit = {
    if (checkoutCorpButtons.length >= 3) return
    val currentSharesMap: Map[Int, Int] = selectedCorps
    val updatedSharesMap: Map[Int, Int] = currentSharesMap.updated(corp,
      if (currentSharesMap.contains(corp)) currentSharesMap(corp) + 1 else 1)
    val correspondingMove: Move = BuyShares(engine.state.currentPlayer, updatedSharesMap)
    if (engine.state.isLegalMove(correspondingMove))
      super.addCheckoutCorpButton(corp)
  }
}
