package BomberMan.model;

/**
 * Représente une explosion temporaire sur la grille dans BomberMan.
 * Une explosion a une position (ligne, colonne) et un timer avant de disparaître.
 */
public class Explosion {
    /** Ligne où se trouve l'explosion. */
    private int row;
    /** Colonne où se trouve l'explosion. */
    private int col;
    /** Timer avant disparition de l'explosion (en ticks, typiquement 30). */
    private int timer = 30;

    /**
     * Construit une explosion à la position donnée.
     * @param row Ligne de l'explosion
     * @param col Colonne de l'explosion
     */
    public Explosion(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Retourne la ligne de l'explosion.
     * @return Ligne (row)
     */
    public int getRow() { return row; }

    /**
     * Retourne la colonne de l'explosion.
     * @return Colonne (col)
     */
    public int getCol() { return col; }

    /**
     * Retourne le timer restant avant disparition.
     * @return Timer (en ticks)
     */
    public int getTimer() { return timer; }

    /**
     * Décrémente le timer de l'explosion d'un tick.
     */
    public void tick() { timer--; }
}