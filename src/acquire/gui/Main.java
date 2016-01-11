package acquire.gui;

import acquire.engine.Engine;
import acquire.gui.theatre.World;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {

        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Acquire");
        scene.getStylesheets().add("acquire/gui/stylesheet.css"); // skin ui elements

        Canvas canvas = new Canvas(1024, 768);
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        scene.setOnMouseEntered(event -> MouseUtil.isMouseInScene = true);
        scene.setOnMouseExited(event -> MouseUtil.isMouseInScene = false);
        scene.setOnMouseClicked(event -> {
            MouseUtil.isMouseClicked = true;
            MouseUtil.setClickPosition(event.getSceneX(), event.getSceneY());
        });
        scene.setOnMousePressed(event -> MouseUtil.isMousePressed = true);
        scene.setOnMouseReleased(event -> MouseUtil.isMousePressed = false);
        scene.setOnMouseMoved(event -> MouseUtil.setPosition(event.getSceneX(), event.getSceneY()));


        Engine engine = Default.newEngine();

        World world = new World();


        Board board = new Board(engine);
        world.addActor(board, 10, 10);

        ScoreSheet sheet = new ScoreSheet(engine);
        world.addActor(sheet, 684, 34);

        History history = new History(engine);
        world.addActor(history, 10, 470);
        history.addToGroup(root);

        AcquireGame game = new AcquireGame(engine, board, sheet);

        world.addActor(game, 0, 0);


        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                // background
                gc.clearRect(0, 0, 1024, 768);
                gc.setFill(new Color(0.2, 0.2, 0.2, 1));
                gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
                gc.setFill(Color.web("707070"));
                gc.fillText(Default.VersionString(), 650, 20);

                world.updateActors();
                world.drawActors(gc);
            }
        }.start();

        stage.show();
    }
}