package BomberMan;

/**
 * Enumération des directions possibles pour un joueur ou une entité dans BomberMan.
 * - DOS : vers le haut (arrière)
 * - FACE : vers le bas (avant)
 * - GAUCHE : vers la gauche
 * - DROITE : vers la droite
 */
public enum Direction {
    /** Vers le haut (dos du personnage). */
    DOS,
    /** Vers le bas (face du personnage). */
    FACE,
    /** Vers la gauche. */
    GAUCHE,
    /** Vers la droite. */
    DROITE
}