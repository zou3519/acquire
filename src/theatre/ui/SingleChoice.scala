package theatre.ui

import javafx.scene.paint.Color

class SingleChoice(val choices: IndexedSeq[String], width: Double, height: Double, buttonFill: Color, textFill: Color)
  extends Button(width, height, buttonFill, textFill, ""){

  private var choice: Int = 0
  private def numChoices: Int = choices.length
  init()

  def init() = {
    textString = choices(choice)
    this.registerClickHandler((Unit) => {
      choice = (choice + 1) % numChoices
      textString = choices(choice)
    })
  }

  def getChoice = choices(choice)

}
