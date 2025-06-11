package BomberMan.model;

/**
 * Enumération représentant les différents types de tuiles dans BomberMan.
 * - WALL : Mur indestructible
 * - FLOOR : Sol (libre, traversable)
 * - BREAKABLE : Mur cassable par une bombe
 */
public enum Tile {
    /** Mur indestructible. */
    WALL,
    /** Sol libre, traversable. */
    FLOOR,
    /** Mur cassable par une explosion. */
    BREAKABLE
}