package acquire.gui;

import acquire.engine.Engine;
import acquire.state.AcquireState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Example extends Application {
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
        for (int i = 0; i < 150; i++) {
            engine.makeMove(engine.state().randomMove().get());
        }

        List<Actor> actors = new ArrayList<>();
        Board board = new Board(engine);
        board.setPosition(10,10);
        ScoreSheet sheet = new ScoreSheet(engine);
        sheet.setPosition(684, 34);

        History history = new History(engine);
        history.setPosition(10, 470);
        history.addToGroup(root);

        actors.add(sheet);
        actors.add(board);
        actors.add(history);

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                // background
                gc.clearRect(0, 0, 1024, 768);
                gc.setFill(new Color(0.2, 0.2, 0.2, 1));
                gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());

                for (Actor actor: actors) {
                    actor.update();
                }
                for (Actor actor : actors) {
                    actor.draw(gc);
                }
            }
        }.start();

        stage.show();
    }
}