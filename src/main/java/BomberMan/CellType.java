package BomberMan;

/**
 * Enumération des différents types de cases (cellules) dans le jeu BomberMan.
 */
public enum CellType {
    /** Case vide, accessible. */
    EMPTY,
    /** Mur indestructible. */
    WALL,
    /** Mur destructible par une bombe. */
    DESTRUCTIBLE_WALL,
    /** Position du joueur 1. */
    PLAYER1,
    /** Position du joueur 2. */
    PLAYER2,
    /** Bombe posée par le joueur 1. */
    BOMB1,
    /** Bombe posée par le joueur 2. */
    BOMB2,
    /** Case actuellement en explosion. */
    EXPLOSION,
    /** Bonus : augmente la portée des bombes. */
    BONUS_RANGE,
    /** Malus : diminue la portée des bombes. */
    MALUS_RANGE,
    /** Position de l'IA. */
    AI
}