package business;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private final List<Player> players;
    private final List<Tile> tiles;
    private int currentPlayerIndex = 0;

    public GameState(List<String> playerNames) {
        this.players = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            String name = playerNames.get(i);
            players.add(new Player(i, BoardUtils.getPlayerColor(i), name));
        }

        this.tiles = BoardUtils.generateTiles();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (players.get(currentPlayerIndex).isBankrupt());
    }

    public Tile getTileAt(int index) {
        return tiles.get(index % 40);
    }

    public List<Player> getActivePlayers() {
        List<Player> active = new ArrayList<>();
        for (Player p : players) {
            if (!p.isBankrupt()) {
                active.add(p);
            }
        }
        return active;
    }

}
