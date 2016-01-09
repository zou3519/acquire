package acquire.gui

import acquire.gui.theatre.Actor

trait ClickableActor extends Actor {
  protected var _clickHandler: (Unit) => Unit = (Unit) => ()

  def registerClickHandler(handler: Unit => Unit): Unit = _clickHandler = handler
  def removeClickHandler(): Unit = _clickHandler = (Unit) => ()

  override def update(): Unit = {
    super.update()
    if (MouseUtil.mouseClicked(this)) _clickHandler()
  }
}
