package business;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class MinimalTest extends Application {

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();

        // 3D scéna (jen Box)
        Group group3D = new Group();
        Box box = new Box(100,100,100);
        group3D.getChildren().add(box);

        // Kamera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
                new Rotate(-45, Rotate.X_AXIS),
                new Translate(0, 0, -300)
        );

        SubScene subScene = new SubScene(group3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.LIGHTBLUE);

        // KLÍČ:
        subScene.setPickOnBounds(false);

        // Tlačítko
        Button btn = new Button("Klikni mě");
        btn.setOnAction(e -> System.out.println("Klik funguje!"));

        // Přidáme do root
        root.getChildren().addAll(subScene, btn);

        // Nastav pořadí
        subScene.toBack();
        btn.toFront();

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
