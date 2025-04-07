package business;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private int selectedPlayers = 2;                  // Výchozí počet hráčů
    private final List<TextField> nameFields = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 600, 400);

        Label title = new Label("Zadej počet hráčů:");
        ToggleGroup group = new ToggleGroup();

        // Řádek s toggly pro 2–4 hráče
        HBox toggles = new HBox(10);
        toggles.setAlignment(Pos.CENTER);

        for (int i = 2; i <= 4; i++) {
            ToggleButton btn = new ToggleButton(i + " hráči");
            btn.setToggleGroup(group);
            if (i == 2) btn.setSelected(true); // výchozí
            btn.setUserData(i);
            btn.setOnAction(e -> {
                selectedPlayers = (int) btn.getUserData();
                refreshNameFields(root);
            });
            toggles.getChildren().add(btn);
        }

        root.getChildren().addAll(title, toggles);

        // Dynamicky generujeme jména hráčů
        refreshNameFields(root);

        // Tlačítko "Spustit hru"
        Button startBtn = new Button("Spustit hru");
        startBtn.setOnAction(e -> {
            // Z každého jméno textfieldu sebere text
            List<String> playerNames = new ArrayList<>();
            for (TextField tf : nameFields) {
                String name = tf.getText().trim();
                if (name.isEmpty()) {
                    name = "Hráč";
                }
                playerNames.add(name);
            }

            // Vytvoří instanci GameScene s těmito jmény
            GameScene game = new GameScene(stage, playerNames);
            // Nastavíme novou scénu
            stage.setScene(game.getScene());
        });

        root.getChildren().add(startBtn);

        stage.setTitle("Monopoly – Počet hráčů");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Pomocná metoda – vymaže staré text fieldy a vytvoří je znovu
     * podle zvoleného počtu hráčů.
     */
    private void refreshNameFields(VBox root) {
        // Smažeme staré textfieldy (pokud tam nějaké byly)
        // Ponecháme v root vše kromě toggles / start buttonu / labelu – proto to řešíme ručně
        nameFields.clear();
        root.getChildren().removeIf(node -> node instanceof TextField);

        for (int i = 1; i <= selectedPlayers; i++) {
            TextField tf = new TextField("Hráč " + i);
            tf.setPromptText("Jméno hráče " + i);
            nameFields.add(tf);
            root.getChildren().add(root.getChildren().size() - 1, tf);
            // vkládáme textfieldy "nahoru" před tlačítko start
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
