package business;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class UI extends Application {

    private static final String[] tileNames = {
            "START", "Náměstí Republiky", "ŠANCE", "Vodičkova", "DAŇ",
            "Hlavní nádraží", "Karlín", "Pokladna", "Žižkov", "Vršovice",
            "VEZENÍ", "Smíchov", "Elektřina", "Holešovice", "Malá Strana",
            "Masarykovo nádr.", "Dejvice", "ŠANCE", "Pankrác", "Pankrác Tower",
            "PARKOVIŠTĚ", "Florenc", "Pokladna", "Vysočany", "Libeň",
            "Hlavní pošta", "Karlovo náměstí", "Voda", "I. P. Pavlova", "Anděl",
            "DO VĚZENÍ", "Letná", "ŠANCE", "Letiště Ruzyně", "Bubeneč",
            "Hradčany", "Pokladna", "Petřiny", "ŠANCE", "Vítězné náměstí"
    };

    @Override
    public void start(Stage stage) {
        double width = Screen.getPrimary().getBounds().getWidth();
        double height = Screen.getPrimary().getBounds().getHeight();

        Group root3D = new Group();

        // 💡 Světlo
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(-500);
        light.setTranslateZ(-500);
        root3D.getChildren().add(light);

        // 📦 Políčka
        int tileCount = 40;
        double boardSize = 600;
        double tileSize = boardSize / 11.0;
        double tileHeight = 12;
        double offset = boardSize / 2 - tileSize / 2;

        PhongMaterial whiteMat = new PhongMaterial(Color.WHITE);
        PhongMaterial blackMat = new PhongMaterial(Color.BLACK);

        for (int i = 0; i < tileCount; i++) {
            double x = 0, z = 0, rotationY = 0;

            if (i < 10) { // spodní
                x = offset - i * tileSize;
                z = offset;
                rotationY = 0;
            } else if (i < 20) { // levá
                x = -offset;
                z = offset - (i - 10) * tileSize;
                rotationY = -90;
            } else if (i < 30) { // horní
                x = -offset + (i - 20) * tileSize;
                z = -offset;
                rotationY = 180;
            } else { // pravá
                x = offset;
                z = -offset + (i - 30) * tileSize;
                rotationY = 90;
            }

            // Bílé políčko
            Box whiteBox = new Box(tileSize * 0.96, tileHeight, tileSize * 0.96);
            whiteBox.setMaterial(whiteMat);
            whiteBox.setTranslateX(x);
            whiteBox.setTranslateY(-tileHeight / 2);
            whiteBox.setTranslateZ(z);
            root3D.getChildren().add(whiteBox);

            // Černý border – 4 okraje
            double b = 1.0;
            Box top = new Box(tileSize, tileHeight, b);
            Box bottom = new Box(tileSize, tileHeight, b);
            Box left = new Box(b, tileHeight, tileSize);
            Box right = new Box(b, tileHeight, tileSize);

            top.setMaterial(blackMat);
            bottom.setMaterial(blackMat);
            left.setMaterial(blackMat);
            right.setMaterial(blackMat);

            top.setTranslateX(x);
            top.setTranslateY(-tileHeight / 2);
            top.setTranslateZ(z - tileSize / 2 + b / 2);

            bottom.setTranslateX(x);
            bottom.setTranslateY(-tileHeight / 2);
            bottom.setTranslateZ(z + tileSize / 2 - b / 2);

            left.setTranslateX(x - tileSize / 2 + b / 2);
            left.setTranslateY(-tileHeight / 2);
            left.setTranslateZ(z);

            right.setTranslateX(x + tileSize / 2 - b / 2);
            right.setTranslateY(-tileHeight / 2);
            right.setTranslateZ(z);

            root3D.getChildren().addAll(top, bottom, left, right);

            // 🏷 Text – položený a natočený správně
            Text label = new Text(tileNames[i]);
            label.setFont(Font.font("Arial", 8));
            label.setFill(Color.BLACK);

            // Otočení textu: leží vodorovně a směřuje správně
            Rotate rotateX = new Rotate(-90, Rotate.X_AXIS);
            Rotate rotateY = new Rotate(rotationY, Rotate.Y_AXIS);
            label.getTransforms().addAll(rotateY, rotateX);

            // Umístění textu na plošku
            label.setTranslateX(x);
            label.setTranslateY(-tileHeight / 2 + 0.2);
            label.setTranslateZ(z);

            root3D.getChildren().add(label);
        }

        // 🎥 Kamera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setFieldOfView(25);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);

        camera.getTransforms().addAll(
                new Rotate(-6345.6, Rotate.Y_AXIS),
                new Rotate(4281.8, Rotate.X_AXIS),
                new Translate(0.0, 45.0, -1500)
        );

        SubScene subScene = new SubScene(root3D, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.LIGHTBLUE);

        Group root = new Group(subScene);
        Scene scene = new Scene(root, width, height, true);

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setTitle("Monopoly – text ležící na políčkách");
        stage.show();
    }
}
