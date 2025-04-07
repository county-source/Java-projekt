package business;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final int id;
    private final String name;
    private int position = 0;
    private int money = 1500;
    private final Sphere figure;

    private boolean bankrupt = false;

    private boolean inJail = false;
    private int jailTurns = 0;

    private final List<Tile> ownedProperties = new ArrayList<>();

    public Player(int id, Color color, String name) {
        this.id = id;
        this.name = name;
        this.figure = new Sphere(BoardUtils.TILE_SIZE / 4.5);
        this.figure.setMaterial(new PhongMaterial(color));
        this.figure.setTranslateY(-BoardUtils.TILE_HEIGHT - 3);
    }


    public void move(int steps) {
        if (inJail) return;
        position = (position - steps + 40) % 40;
    }

    public void moveTo(int index) {
        position = index % 40;
    }

    public void setBankrupt() {
        bankrupt = true;
        ownedProperties.clear();
    }

    public boolean isBankrupt() {
        return bankrupt;
    }

    public boolean isInJail() {
        return inJail;
    }

    public void goToJail() {
        inJail = true;
        jailTurns = 3;
        moveTo(10);
    }

    public void jailTurn() {
        if (jailTurns > 0) {
            jailTurns--;
            if (jailTurns == 0) inJail = false;
        }
    }

    public void freeFromJail() {
        inJail = false;
        jailTurns = 0;
    }

    public void buyProperty(Tile tile) {
        ownedProperties.add(tile);
    }

    public List<Tile> getOwnedProperties() {
        return ownedProperties;
    }

    public int getPosition() {
        return position;
    }

    public Sphere getFigure() {
        return figure;
    }

    public int getId() {
        return id;
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int amount) {
        money += amount;
    }

    public void subtractMoney(int amount) {
        money -= amount;
        if (money < 0) setBankrupt();
    }

    public String getName() {
        return name;
    }
}
