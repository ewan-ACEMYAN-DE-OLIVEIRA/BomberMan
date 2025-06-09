package BomberMan.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private final GameMap map;
    private final List<Player> players;
    private final List<Bomb> bombs;
    private final List<Explosion> explosions;

    public GameState(GameMap map, boolean is1v1) {
        this.map = map;
        this.players = new ArrayList<>();
        this.bombs = new ArrayList<>();
        this.explosions = new ArrayList<>();

        // Position de spawn joueur 1 (adapte le nom et la position si tu veux)
        players.add(new Player("P1", 1, 1, 3));
        if (is1v1) {
            // Position de spawn joueur 2 (coin opposé, adapte si tu veux)
            players.add(new Player("P2", map.getHeight() - 2, map.getWidth() - 2, 3));
        }
    }

    public GameMap getMap() {
        return map;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Bomb> getBombs() {
        return bombs;
    }

    public List<Explosion> getExplosions() {
        return explosions;
    }
}