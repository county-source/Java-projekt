package business;

import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScene {

    private final Scene scene;
    private final GameState gameState;
    private final Random random = new Random();

    // Kostka (slot-machine animace)
    private final Label rollingNumber = new Label("?");
    // Log větší, tučný, zarovnaný pod tlačítkem
    private final Label logLabel = new Label("Hra začíná!");

    private final VBox playerStats = new VBox(5);
    private final Group root3D = new Group();
    private final StackPane root = new StackPane();

    private final List<Label> cornerLabels = new ArrayList<>();
    private Box highlightBox = null;

    public GameScene(Stage stage, List<String> playerNames) {
        // 1) GameState
        gameState = new GameState(playerNames);

        // 2) Vykreslení 3D
        BoardUtils.addBoardTiles(root3D, gameState.getTiles());
        BoardUtils.addLighting(root3D);
        for (Player p : gameState.getPlayers()) {
            root3D.getChildren().add(p.getFigure());
        }
        BoardUtils.positionPlayers(gameState.getPlayers(), gameState);

        // Kamera + SubScene
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setFieldOfView(25);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.getTransforms().addAll(
                new Rotate(-6345.6, Rotate.Y_AXIS),
                new Rotate(4281.8, Rotate.X_AXIS),
                new Translate(0.0, 45.0, -1500)
        );
        SubScene subScene = new SubScene(root3D, 1920, 1080, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.LIGHTBLUE);

        subScene.setPickOnBounds(false); // aby šly klikat overlay prvky

        // Slot-machine číslo (velké)
        rollingNumber.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");

        // Log – větší, tučný
        logLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        logLabel.setWrapText(true);
        logLabel.setMaxWidth(600);

        // Tlačítko
        Button rollBtn = new Button("Hodit kostku");
        rollBtn.setStyle("""
            -fx-font-size: 24px;
            -fx-background-color: #2196F3;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """);
        rollBtn.setOnAction(e -> nextTurn());

        // VBox overlay – [ slotNumber, tlačítko, log ]
        VBox overlay = new VBox(15, rollingNumber, rollBtn, logLabel);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(10));

        // Panel se staty hráčů (vpravo nahoře)
        updatePlayerStats();
        playerStats.setStyle("-fx-font-size: 14px;");
        playerStats.setPadding(new Insets(15));
        playerStats.setTranslateX(720);
        playerStats.setTranslateY(-360);

        // root
        root.getChildren().addAll(subScene, overlay, playerStats);

        // Rohové labely
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            Label cornerLabel = new Label();
            cornerLabel.setStyle("""
                -fx-font-size: 16px;
                -fx-background-color: rgba(0,0,0,0.7);
                -fx-text-fill: white;
                -fx-padding: 8px;
            """);
            cornerLabels.add(cornerLabel);

            switch (i) {
                case 0 -> StackPane.setAlignment(cornerLabel, Pos.TOP_LEFT);
                case 1 -> StackPane.setAlignment(cornerLabel, Pos.TOP_RIGHT);
                case 2 -> StackPane.setAlignment(cornerLabel, Pos.BOTTOM_LEFT);
                case 3 -> StackPane.setAlignment(cornerLabel, Pos.BOTTOM_RIGHT);
            }
            root.getChildren().add(cornerLabel);
        }

        scene = new Scene(root, 1920, 1080, true);
        subScene.toBack();
        overlay.toFront();
        playerStats.toFront();
        cornerLabels.forEach(Node::toFront);

        stage.setScene(scene);
        stage.setFullScreen(true);
    }

    /**
     * Spustí slot-machine animaci kostky a poté tile-by-tile pohyb.
     */
    private void nextTurn() {
        List<Player> active = gameState.getActivePlayers();
        if (active.size() == 1) {
            Player winner = active.get(0);
            logLabel.setText("Vítězí " + winner.getName() + " se zůstatkem " + winner.getMoney() + " Kč!");
            return;
        }
        Player player = gameState.getCurrentPlayer();
        if (player.isBankrupt()) {
            logLabel.setText(player.getName() + " je vyřazen.");
            gameState.nextPlayer();
            return;
        }
        if (player.isInJail()) {
            player.jailTurn();
            logLabel.setText(player.getName() + " je ve vězení. Zbývá " + (player.isInJail() ? "další tah." : "volný!"));
            gameState.nextPlayer();
            return;
        }

        rollingNumber.setText("?");
        final int steps = 10;
        Timeline timeline = new Timeline();
        for (int i = 0; i < steps; i++) {
            KeyFrame kf = new KeyFrame(Duration.millis((i+1)*100), e -> {
                int fake = random.nextInt(6)+1;
                rollingNumber.setText(String.valueOf(fake));
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.setOnFinished(e -> {
            int finalRoll = random.nextInt(6)+1;
            rollingNumber.setText(String.valueOf(finalRoll));

            doDiceResult(finalRoll);
        });
        timeline.play();
    }

    /**
     * Po animaci kostky – figurka se pohne pole-po-poli.
     */
    private void doDiceResult(int roll) {
        Player player = gameState.getCurrentPlayer();
        logLabel.setText(player.getName() + " hodil: " + roll);

        // Zjistíme starou a novou pozici (už nastavenou v Player? – Nyní ne!)
        // Místo standard: player.move(roll); => Děláme tile-by-tile
        // => rovnou tileByTileMovement

        tileByTileMovement(player, roll, () -> {
            // Po dokončení pohybu...
            int newPos = player.getPosition(); // teď je posunut
            Tile tile = gameState.getTileAt(newPos);
            BoardUtils.positionPlayers(gameState.getPlayers(), gameState);

            if (tile.getType() == TileType.NEMOVITOST) {
                showPropertyMenu(player, tile);
            } else {
                handleTile(player, tile);
                updatePlayerStats();
                gameState.nextPlayer();
            }
        });
    }

    /**
     * Pohyb figurky "pole po poli" – 1 tilesize animace x 'roll' krát.
     * Po dokončení volá onFinished.
     */
    private void tileByTileMovement(Player player, int steps, Runnable onFinished) {
        // Zjistíme, zda jdeme dopředu nebo dozadu:
        // V Player je logika, jestli position roste či klesá (možná).
        // Tady budeme "ručně" krokovat. Předpokládám, že v Player se pohyb standardně
        // dělá (pos - roll + 40)%40 => proti směru. Můžeš upravit:

        // Nebudeme teď měnit player.position najednou,
        // ale pohneme se "krok-za-krokem" v animaci a player se aktualizuje postupně.

        SequentialTransition seq = new SequentialTransition();

        for (int i = 0; i < steps; i++) {
            // 1 krok
            // posun v Player
            player.move(1); // voláme move(1) => posune se o 1 pole
            int newPos = player.getPosition();

            // spočítáme souřadnice
            double[] pos = BoardUtils.getTilePosition(newPos);

            // TranslateTransition pro 1 krok
            TranslateTransition tt = new TranslateTransition(Duration.seconds(0.3), player.getFigure());
            tt.setToX(pos[0]);
            tt.setToZ(pos[1]);
            // Ve finále highlight
            tt.setOnFinished(e -> {
                removeHighlight();
                highlightTile(newPos);
            });
            seq.getChildren().add(tt);
        }

        // Po posledním
        seq.setOnFinished(e -> onFinished.run());

        seq.play();
    }

    // Odstranění highlight
    private void removeHighlight() {
        if (highlightBox != null) {
            root3D.getChildren().remove(highlightBox);
            highlightBox = null;
        }
    }

    // Zvýrazní dané pole (žlutým poloprůhledným čtvercem)
    private void highlightTile(int index) {
        double[] pos = BoardUtils.getTilePosition(index);
        highlightBox = new Box(BoardUtils.TILE_SIZE * 1.1, 1, BoardUtils.TILE_SIZE * 1.1);
        highlightBox.setTranslateX(pos[0]);
        highlightBox.setTranslateZ(pos[1]);
        highlightBox.setTranslateY(-BoardUtils.TILE_HEIGHT - 0.1);

        PhongMaterial mat = new PhongMaterial(Color.color(1, 1, 0, 0.4));
        highlightBox.setMaterial(mat);
        root3D.getChildren().add(highlightBox);
    }

    /**
     * Hezký malý horizontální panel pro "Nakup" / "Zavřít".
     */
    private void showPropertyMenu(Player player, Tile tile) {
        HBox menu = new HBox(20);
        menu.setPadding(new Insets(15));
        menu.setStyle("""
            -fx-background-color: #333333cc;
            -fx-background-radius: 10;
            -fx-border-color: white;
            -fx-border-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8,0,4,4);
        """);
        menu.setMaxSize(400, 100);
        menu.setAlignment(Pos.CENTER_LEFT);

        VBox textPart = new VBox(8);
        textPart.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Nemovitost: " + tile.getName());
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label priceLabel = new Label();
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: lightgreen;");
        textPart.getChildren().addAll(title, priceLabel);

        VBox btnPart = new VBox(8);
        btnPart.setAlignment(Pos.CENTER_RIGHT);

        Button actionBtn = new Button();
        actionBtn.setStyle("""
            -fx-font-size: 14px;
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-padding: 6 12;
        """);

        Button closeBtn = new Button("Zavřít");
        closeBtn.setStyle("""
            -fx-font-size: 14px;
            -fx-background-color: #555555;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-padding: 6 12;
        """);
        closeBtn.setOnAction(e -> {
            root.getChildren().remove(menu);
            logLabel.setText(player.getName() + " odmítl akci na poli.");
            updatePlayerStats();
            gameState.nextPlayer();
        });

        btnPart.getChildren().addAll(actionBtn, closeBtn);
        menu.getChildren().addAll(textPart, btnPart);

        StackPane.setAlignment(menu, Pos.CENTER);

        if (!tile.isOwned()) {
            priceLabel.setText("Cena: " + tile.getPrice() + " Kč");
            priceLabel.setTextFill(Color.LIGHTGREEN);

            actionBtn.setText("Koupit");
            actionBtn.setOnAction(e -> {
                if (player.getMoney() >= tile.getPrice()) {
                    tile.setOwner(player);
                    player.buyProperty(tile);
                    player.subtractMoney(tile.getPrice());
                    addHouseToBoard(tile);
                    logLabel.setText(player.getName() + " koupil " + tile.getName());
                } else {
                    logLabel.setText(player.getName() + " nemá dost peněz.");
                }
                root.getChildren().remove(menu);
                updatePlayerStats();
                gameState.nextPlayer();
            });

        } else if (tile.getOwner() == player && tile.canUpgrade()) {
            int cost = tile.getUpgradeCost();
            priceLabel.setText("Upgrade: " + cost + " Kč (úroveň " + tile.getLevel() + ")");
            priceLabel.setTextFill(Color.CYAN);

            actionBtn.setText("Upgradovat");
            actionBtn.setStyle("""
                -fx-font-size: 14px;
                -fx-background-color: #FF9800;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 6 12;
            """);
            actionBtn.setOnAction(e -> {
                if (player.getMoney() >= cost) {
                    player.subtractMoney(cost);
                    tile.upgrade();
                    addHouseToBoard(tile);
                    logLabel.setText(player.getName() + " vylepšil " + tile.getName() + " na úroveň " + tile.getLevel());
                } else {
                    logLabel.setText("Nedostatek peněz na upgrade.");
                }
                root.getChildren().remove(menu);
                updatePlayerStats();
                gameState.nextPlayer();
            });

        } else {
            logLabel.setText("Žádná akce dostupná.");
            return;
        }

        root.getChildren().add(menu);
    }

    private void handleTile(Player player, Tile tile) {
        StringBuilder log = new StringBuilder(player.getName() + " → " + tile.getName());

        switch (tile.getType()) {
            case DAN -> {
                player.subtractMoney(200);
                log.append(" zaplatil daň 200 Kč.");
            }
            case START -> {
                player.addMoney(500);
                log.append(" získal 500 Kč za průchod START.");
            }
            case DO_VEZENI -> {
                player.goToJail();
                log.append(" jde do vězení.");
            }
            case SANCE, POKLADNA -> {
                ChanceCard.applyRandomEffect(player, gameState);
                log.append(" karta šance/pokladna.");
            }
            case VEZENI, PARKOVISTE -> log.append(" odpočívá.");
        }

        if (player.isBankrupt()) {
            log.append(" 💀 ").append(player.getName()).append(" zkrachoval!");
        }

        logLabel.setText(log.toString());
    }

    // Přidání domků
    private void addHouseToBoard(Tile tile) {
        removeOldHouses(tile.getIndex());
        double[] pos = BoardUtils.getTilePosition(tile.getIndex());
        double x = pos[0];
        double z = pos[1];
        int level = tile.getLevel();

        for (int i = 0; i < level; i++) {
            Box house = new Box(10, 10, 10);
            house.setTranslateX(x - 15 + i * 15);
            house.setTranslateY(-BoardUtils.TILE_HEIGHT - 5);
            house.setTranslateZ(z);
            house.setMaterial(new PhongMaterial(Color.DARKGREEN));
            root3D.getChildren().add(house);
        }
    }

    // Smazání starých domků
    private void removeOldHouses(int tileIndex) {
        List<Node> toRemove = new ArrayList<>();
        for (Node n : root3D.getChildren()) {
            if (n instanceof Box box) {
                double bx = box.getTranslateX();
                double bz = box.getTranslateZ();
                double[] pos = BoardUtils.getTilePosition(tileIndex);
                if (Math.abs(bx - pos[0]) < 40 && Math.abs(bz - pos[1]) < 40 &&
                        box.getMaterial() instanceof PhongMaterial pm) {
                    if (pm.getDiffuseColor().equals(Color.DARKGREEN)) {
                        toRemove.add(box);
                    }
                }
            }
        }
        root3D.getChildren().removeAll(toRemove);
    }

    // Obnovení panelu hráčů + rohových labelů
    private void updatePlayerStats() {
        playerStats.getChildren().clear();
        for (Player p : gameState.getPlayers()) {
            StringBuilder sb = new StringBuilder(p.getName());
            sb.append(p.isBankrupt() ? " ❌" : "").append(" — ").append(p.getMoney()).append(" Kč");

            if (!p.getOwnedProperties().isEmpty()) {
                sb.append("\n  → ");
                for (Tile t : p.getOwnedProperties()) {
                    sb.append(t.getName()).append("(L").append(t.getLevel()).append("), ");
                }
                sb.setLength(sb.length() - 2);
            }

            Label info = new Label(sb.toString());
            info.setTextFill(p.isBankrupt() ? Color.GRAY : Color.WHITE);
            playerStats.getChildren().add(info);
        }

        // Rohy
        for (int i = 0; i < cornerLabels.size(); i++) {
            Player p = gameState.getPlayers().get(i);
            String text = p.getName() + "\n" + (p.isBankrupt() ? "BANKROT" : (p.getMoney() + " Kč"));
            Label label = cornerLabels.get(i);
            label.setText(text);

            if (p.isBankrupt()) {
                label.setStyle("""
                    -fx-font-size: 16px;
                    -fx-background-color: rgba(200,0,0,0.8);
                    -fx-text-fill: white;
                    -fx-padding: 8px;
                """);
            } else {
                label.setStyle("""
                    -fx-font-size: 16px;
                    -fx-background-color: rgba(0,0,0,0.7);
                    -fx-text-fill: white;
                    -fx-padding: 8px;
                """);
            }
        }
    }

    public Scene getScene() {
        return scene;
    }
}
