package BomberMan.model;

/**
 * Représente une bombe posée sur la grille dans BomberMan.
 * Contient sa position, son timer (avant explosion), sa portée et son propriétaire.
 */
public class Bomb {
    /** Ligne où se trouve la bombe. */
    private int row;
    /** Colonne où se trouve la bombe. */
    private int col;
    /** Timer avant explosion (en ticks, typiquement 60 = 1 seconde). */
    private int timer = 60;
    /** Portée de l'explosion de la bombe. */
    private int range;
    /** Joueur propriétaire de la bombe. */
    private Player owner;

    /**
     * Construit une bombe à la position donnée, posée par le joueur indiqué.
     * Le rayon d'explosion est celui du joueur au moment de la pose.
     * @param row Ligne où placer la bombe
     * @param col Colonne où placer la bombe
     * @param owner Joueur qui a posé la bombe
     */
    public Bomb(int row, int col, Player owner) {
        this.row = row;
        this.col = col;
        this.owner = owner;
        this.range = owner.getBombRange(); // Prend le rayon actuel du joueur
    }

    /**
     * Décrémente le timer de la bombe d'un tick.
     */
    public void tick() { timer--; }

    /**
     * Retourne le timer restant avant l'explosion.
     * @return Timer (en ticks)
     */
    public int getTimer() { return timer; }

    /**
     * Retourne la ligne de la bombe.
     * @return Ligne (row)
     */
    public int getRow() { return row; }

    /**
     * Retourne la colonne de la bombe.
     * @return Colonne (col)
     */
    public int getCol() { return col; }

    /**
     * Retourne la portée de l'explosion de la bombe.
     * @return Portée (range)
     */
    public int getRange() { return range; }

    /**
     * Retourne le propriétaire (joueur) de la bombe.
     * @return Joueur propriétaire
     */
    public Player getOwner() { return owner; }
}