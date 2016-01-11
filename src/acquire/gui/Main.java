package acquire.gui;

import acquire.engine.Engine;
import acquire.gui.acquireUI.*;
import acquire.gui.theatre.MouseUtil;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Engine engine = Default.newEngine();
        AcquireWorld world = new AcquireWorld(engine);

        stage.setTitle("Acquire");
        stage.setScene(world.scene());

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                world.updateWorld();
                world.drawWorld();
            }
        }.start();

        stage.show();
    }
}