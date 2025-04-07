package business;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.ArrayList;
import java.util.List;

public class BoardUtils {

    public static final double TILE_SIZE = 600 / 11.0;
    public static final double TILE_HEIGHT = 12;
    public static final double OFFSET = 600 / 2 - TILE_SIZE / 2;

    public static void addBoardTiles(Group root, List<Tile> tiles) {
        for (Tile tile : tiles) {
            double[] pos = getTilePosition(tile.getIndex());
            double x = pos[0];
            double z = pos[1];

            Box tileBox = new Box(TILE_SIZE * 0.96, TILE_HEIGHT, TILE_SIZE * 0.96);
            tileBox.setTranslateX(x);
            tileBox.setTranslateY(-TILE_HEIGHT / 2);
            tileBox.setTranslateZ(z);
            tileBox.setMaterial(new PhongMaterial(getTileColor(tile.getType())));
            root.getChildren().add(tileBox);

            // okraje
            double b = 1.0;
            root.getChildren().addAll(
                    createBorder(TILE_SIZE, TILE_HEIGHT, b, x, z - TILE_SIZE / 2 + b / 2),
                    createBorder(TILE_SIZE, TILE_HEIGHT, b, x, z + TILE_SIZE / 2 - b / 2),
                    createBorder(b, TILE_HEIGHT, TILE_SIZE, x - TILE_SIZE / 2 + b / 2, z),
                    createBorder(b, TILE_HEIGHT, TILE_SIZE, x + TILE_SIZE / 2 - b / 2, z)
            );
        }
    }

    private static Color getTileColor(TileType type) {
        return switch (type) {
            case START -> Color.LIGHTGREEN;
            case NEMOVITOST -> Color.BEIGE;
            case SANCE -> Color.CORNFLOWERBLUE;
            case DAN -> Color.DARKRED;
            case VEZENI -> Color.GRAY;
            case PARKOVISTE -> Color.LIGHTYELLOW;
            case DO_VEZENI -> Color.ORANGE;
            case POKLADNA -> Color.PINK;
        };
    }

    private static Box createBorder(double width, double height, double depth, double x, double z) {
        Box border = new Box(width, height, depth);
        border.setMaterial(new PhongMaterial(Color.BLACK));
        border.setTranslateX(x);
        border.setTranslateY(-height / 2);
        border.setTranslateZ(z);
        return border;
    }

    public static void addLighting(Group root) {
        javafx.scene.PointLight light = new javafx.scene.PointLight(Color.WHITE);
        light.setTranslateY(-500);
        light.setTranslateZ(-500);
        root.getChildren().add(light);
    }

    public static double[] getTilePosition(int index) {
        double x = 0, z = 0;
        if (index < 10) {
            x = OFFSET - index * TILE_SIZE;
            z = OFFSET;
        } else if (index < 20) {
            x = -OFFSET;
            z = OFFSET - (index - 10) * TILE_SIZE;
        } else if (index < 30) {
            x = -OFFSET + (index - 20) * TILE_SIZE;
            z = -OFFSET;
        } else {
            x = OFFSET;
            z = -OFFSET + (index - 30) * TILE_SIZE;
        }
        return new double[]{x, z};
    }

    // Přemístěno: zobrazení hráče
    public static void positionPlayers(List<Player> players, GameState state) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int tile = p.getPosition();

            double[] pos = getTilePosition(tile);
            double x = pos[0];
            double z = pos[1];

            double spread = TILE_SIZE / 3.0;
            double fx = x - spread / 2 + (i % 2) * spread;
            double fz = z - spread / 2 + (i / 2) * spread;

            p.getFigure().setTranslateX(fx);
            p.getFigure().setTranslateZ(fz);
        }
    }

    public static Color getPlayerColor(int index) {
        return switch (index) {
            case 0 -> Color.RED;
            case 1 -> Color.BLUE;
            case 2 -> Color.GREEN;
            case 3 -> Color.YELLOW;
            default -> Color.GRAY;
        };
    }

    public static List<Tile> generateTiles() {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            TileType type;
            int price = 0;
            int rent = 0;
            String name = "Pole " + i;

            // ukázkové rozdělení
            if (i == 0) type = TileType.START;
            else if (i == 2 || i == 17 || i == 38) type = TileType.SANCE;
            else if (i == 4 || i == 33) type = TileType.DAN;
            else if (i == 10) type = TileType.VEZENI;
            else if (i == 20) type = TileType.PARKOVISTE;
            else if (i == 30) type = TileType.DO_VEZENI;
            else if (i == 7 || i == 22 || i == 36) type = TileType.POKLADNA;
            else {
                type = TileType.NEMOVITOST;
                price = 100 + (i % 5) * 20;
                rent = price / 5;
            }

            tiles.add(new Tile(i, type, name, price, rent));
        }
        return tiles;
    }

    /**
     * Najde Box pro dané tileIndex (pokud chceš měnit textury)
     */
    public static Box getTileBox(Group root3D, int tileIndex) {
        double[] pos = getTilePosition(tileIndex);
        double x = pos[0];
        double z = pos[1];
        for (Node n : root3D.getChildren()) {
            if (n instanceof Box box) {
                // porovnáme X a Z s malou tolerancí
                if (Math.abs(box.getTranslateX() - x) < 1 &&
                        Math.abs(box.getTranslateZ() - z) < 1) {
                    return box;
                }
            }
        }
        return null;
    }
}
