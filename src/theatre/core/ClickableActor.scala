package theatre.core

abstract class ClickableActor extends Actor {
  private var _clickHandler: (Unit) => Unit = (Unit) => ()

  def clickHandler = _clickHandler
  def registerClickHandler(handler: Unit => Unit): Unit = _clickHandler = handler
  def removeClickHandler(): Unit = _clickHandler = (Unit) => ()

  override def update(): Unit = {
    super.update()
    if (MouseUtil.mouseClicked(this)) clickHandler()
  }
}
