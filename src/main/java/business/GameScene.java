package business;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    private final Scene scene;          // Atribut scény (getScene() ho vrátí)
    private final GameState gameState;  // Stav hry
    private final Random random = new Random();

    private final Label diceLabel = new Label("🎲");
    private final Label logLabel = new Label("Hra začíná!");
    private final VBox playerStats = new VBox(5);
    private final Group root3D = new Group();
    private final StackPane root = new StackPane();

    private final List<Label> cornerLabels = new ArrayList<>();
    private Box highlightBox = null;   // Zvýraznění pole
    private final Pane dicePane = new Pane(); // Animace kostky

    public GameScene(Stage stage, List<String> playerNames) {
        // 1) Vytvoření GameState
        gameState = new GameState(playerNames);

        // 2) Vykreslení 3D scény
        BoardUtils.addBoardTiles(root3D, gameState.getTiles());
        BoardUtils.addLighting(root3D);
        for (Player player : gameState.getPlayers()) {
            root3D.getChildren().add(player.getFigure());
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

        // DŮLEŽITÉ: SubScene nepohlcuje kliknutí mimo 3D
        subScene.setPickOnBounds(false);

        // Tlačítko Hod kostkou
        Button rollBtn = new Button("🎲 Hod kostkou");
        rollBtn.setOnAction(e -> nextTurn(stage));
        rollBtn.setStyle("""
            -fx-font-size: 24px;
            -fx-background-color: #2196F3;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """);

        // Tlačítko Nápověda
        Button helpBtn = new Button("Nápověda");
        helpBtn.setStyle("""
            -fx-font-size: 18px;
            -fx-background-color: #9C27B0;
            -fx-text-fill: white;
            -fx-padding: 6 15;
            -fx-cursor: hand;
        """);
        helpBtn.setOnAction(e -> showHelp());

        // Tlačítko Restart
        Button restartBtn = new Button("↩ Restart");
        restartBtn.setStyle("""
            -fx-font-size: 18px;
            -fx-background-color: #E91E63;
            -fx-text-fill: white;
            -fx-padding: 6 15;
            -fx-cursor: hand;
        """);
        restartBtn.setOnAction(e -> {
            stage.setFullScreen(false);
            stage.close();
        });

        // Panel tlačítek
        HBox topButtons = new HBox(20, rollBtn, helpBtn, restartBtn);
        topButtons.setAlignment(Pos.CENTER);

        // Kostka + Log
        diceLabel.setTextFill(Color.WHITE);
        diceLabel.setStyle("-fx-font-size: 36px;");
        logLabel.setTextFill(Color.WHITE);
        logLabel.setStyle("-fx-font-size: 16px;");
        logLabel.setWrapText(true);
        logLabel.setMaxWidth(600);

        // VBOX overlay
        VBox overlay = new VBox(20, diceLabel, topButtons, logLabel);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(10));

        // Panel se staty hráčů
        updatePlayerStats();
        playerStats.setStyle("-fx-font-size: 14px;");
        playerStats.setPadding(new Insets(15));
        playerStats.setTranslateX(720);
        playerStats.setTranslateY(-360);

        // Panel pro anim kostky
        dicePane.setPrefSize(200, 200);
        dicePane.setStyle("-fx-background-color: transparent;");
        StackPane.setAlignment(dicePane, Pos.BOTTOM_CENTER);

        // Přidat do root
        root.getChildren().addAll(subScene, overlay, playerStats, dicePane);

        // Rohové panely (jméno + peníze)
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

        // Vytvořit Scene, definovat pořadí
        scene = new Scene(root, 1920, 1080, true);

        // SubScene dozadu, overlay a ostatní dopředu
        subScene.toBack();
        overlay.toFront();
        playerStats.toFront();
        dicePane.toFront();
        for (Label cl : cornerLabels) {
            cl.toFront();
        }

        stage.setScene(scene);
        stage.setFullScreen(true);
    }

    // Metoda zobrazení nápovědy
    private void showHelp() {
        VBox helpBox = new VBox(15);
        helpBox.setPadding(new Insets(20));
        helpBox.setStyle("-fx-background-color: #444444bb; -fx-border-color: white;");
        helpBox.setMaxWidth(400);
        helpBox.setAlignment(Pos.CENTER);

        Label helpTitle = new Label("Nápověda");
        helpTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        Label helpText = new Label("""
            Jak hrát:
            1) Klikni na 'Hod kostkou'.
            2) Pohni figurkou. Pokud stoupíš na nemovitost, můžeš ji koupit.
            3) ŠANCE/POKLADNA dávají bonusy nebo postihy.
            4) Cíl: nepřijít o všechny peníze a vyřadit soupeře.
            """);
        helpText.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        helpText.setWrapText(true);
        helpText.setMaxWidth(380);

        Button closeBtn = new Button("Zavřít");
        closeBtn.setStyle("""
            -fx-background-color: #757575;
            -fx-text-fill: white;
            -fx-padding: 6 20;
        """);
        closeBtn.setOnAction(e -> root.getChildren().remove(helpBox));

        helpBox.getChildren().addAll(helpTitle, helpText, closeBtn);
        StackPane.setAlignment(helpBox, Pos.CENTER);
        root.getChildren().add(helpBox);
    }

    private void nextTurn(Stage stage) {
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

        // Animace kostky
        animateDice();
        int roll = random.nextInt(6) + 1;
        diceLabel.setText("🎲 " + player.getName() + " hodil " + roll);

        int oldPos = player.getPosition();
        player.move(roll);
        int newPos = player.getPosition();

        removeHighlight();
        highlightTile(newPos);

        animateMove(player, oldPos, newPos, () -> {
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

    private void removeHighlight() {
        if (highlightBox != null) {
            root3D.getChildren().remove(highlightBox);
            highlightBox = null;
        }
    }

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

    private void animateDice() {
        dicePane.getChildren().clear();

        Timeline timeline = new Timeline();
        for (int i = 1; i <= 6; i++) {
            final int frame = i;
            KeyFrame kf = new KeyFrame(Duration.millis(i * 100), e -> {
                dicePane.getChildren().clear();
                ImageView iv = new ImageView("file:assets/dice_" + frame + ".png");
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                dicePane.getChildren().add(iv);
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.setCycleCount(1);
        timeline.setOnFinished(e -> dicePane.getChildren().clear());
        timeline.play();
    }

    private void animateMove(Player player, int from, int to, Runnable onFinished) {
        double[] pos = BoardUtils.getTilePosition(to);
        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.6), player.getFigure());
        tt.setToX(pos[0]);
        tt.setToZ(pos[1]);
        tt.setOnFinished(e -> onFinished.run());
        tt.play();
    }

    private void showPropertyMenu(Player player, Tile tile) {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(20));
        menu.setStyle("-fx-background-color: #333333aa; -fx-border-color: white;");
        menu.setMaxWidth(300);
        menu.setAlignment(Pos.CENTER);

        Label title = new Label("Nemovitost: " + tile.getName());
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 18px;");

        Label priceLabel = new Label();
        Button actionBtn = new Button();
        Button closeBtn = new Button("Zavřít");

        closeBtn.setOnAction(e -> {
            root.getChildren().remove(menu);
            logLabel.setText(player.getName() + " odmítl akci na poli.");
            updatePlayerStats();
            gameState.nextPlayer();
        });

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
            root.getChildren().remove(menu);
            updatePlayerStats();
            gameState.nextPlayer();
            return;
        }

        menu.getChildren().addAll(title, priceLabel, actionBtn, closeBtn);
        StackPane.setAlignment(menu, Pos.CENTER);
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

    private void removeOldHouses(int tileIndex) {
        List<Node> toRemove = new ArrayList<>();
        for (Node n : root3D.getChildren()) {
            if (n instanceof Box box) {
                double bx = box.getTranslateX();
                double bz = box.getTranslateZ();
                double[] pos = BoardUtils.getTilePosition(tileIndex);
                if (Math.abs(bx - pos[0]) < 40 && Math.abs(bz - pos[1]) < 40 &&
                        box.getMaterial() instanceof PhongMaterial pm) {
                    // heuristika: barva domku
                    if (pm.getDiffuseColor().equals(Color.DARKGREEN)) {
                        toRemove.add(box);
                    }
                }
            }
        }
        root3D.getChildren().removeAll(toRemove);
    }

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

    // VEŘEJNÁ GET SCENE - pro Main.java: stage.setScene(game.getScene());
    public Scene getScene() {
        return scene;
    }
}
