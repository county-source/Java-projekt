package business;

public class Tile {
    private final int index;
    private final TileType type;
    private final String name;
    private final int price;
    private final int baseRent;

    private Player owner;
    private int level = 0; // 0 = nic, 1–3 = domy

    public Tile(int index, TileType type, String name, int price, int baseRent) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.price = price;
        this.baseRent = baseRent;
    }

    public int getIndex() {
        return index;
    }

    public TileType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public boolean isOwned() {
        return owner != null;
    }

    public int getLevel() {
        return level;
    }

    public boolean canUpgrade() {
        return level < 3 && isOwned();
    }

    public int getUpgradeCost() {
        return price / 2 + level * 50;
    }

    public void upgrade() {
        if (canUpgrade()) {
            level++;
        }
    }

    public int getRent() {
        return baseRent + level * 50;
    }
}
