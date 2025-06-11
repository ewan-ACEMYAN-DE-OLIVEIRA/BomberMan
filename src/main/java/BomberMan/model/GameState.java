package BomberMan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente l'état complet d'une partie de BomberMan à un instant donné :
 * - la carte (map)
 * - la liste des joueurs
 * - la liste des bombes actives
 * - la liste des explosions en cours
 */
public class GameState {
    /** Carte du jeu. */
    private final GameMap map;
    /** Joueurs présents dans la partie. */
    private final List<Player> players;
    /** Bombes actuellement posées sur la carte. */
    private final List<Bomb> bombs;
    /** Explosions en cours d'affichage sur la carte. */
    private final List<Explosion> explosions;

    /**
     * Construit un nouvel état de jeu avec une carte donnée et le mode de jeu choisi.
     * @param map Carte du niveau
     * @param is1v1 true pour deux joueurs humains, false pour un seul joueur
     */
    public GameState(GameMap map, boolean is1v1) {
        this.map = map;
        this.players = new ArrayList<>();
        this.bombs = new ArrayList<>();
        this.explosions = new ArrayList<>();

        players.add(new Player("P1", 1, 1, 3));
        if (is1v1) {

            players.add(new Player("P2", map.getHeight() - 2, map.getWidth() - 2, 3));
        }
    }

    /** Retourne la carte du jeu. */
    public GameMap getMap() {
        return map;
    }

    /** Retourne la liste des joueurs. */
    public List<Player> getPlayers() {
        return players;
    }

    /** Retourne la liste des bombes actives. */
    public List<Bomb> getBombs() {
        return bombs;
    }

    /** Retourne la liste des explosions en cours. */
    public List<Explosion> getExplosions() {
        return explosions;
    }
}