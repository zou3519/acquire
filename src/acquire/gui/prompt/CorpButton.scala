package acquire.gui.prompt

import javafx.scene.paint.Color

import acquire.engine.Engine
import acquire.gui.{Button, Colors}

// TODO: remove engine dependency
class CorpButton(engine: Engine, val corp: Int) extends
  Button(40, 40, Colors.colors(corp+2), Color.web("606060"),
    new String(engine.state.config.corpName(corp).toCharArray.take(2)))
