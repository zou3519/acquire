package acquire.gui

import javafx.scene.paint.Color

import acquire.engine.Engine

class CorpButton(engine: Engine, val corp: Int) extends
  Button(40, 40, Colors.colors(corp+2), Color.web("707070"),
    new String(engine.state.config.corpName(corp).toCharArray.take(2)))
