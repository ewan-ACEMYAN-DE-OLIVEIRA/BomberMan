package BomberMan.model;

/**
 * Représente un joueur dans BomberMan.
 * Gère le nom, la position, le nombre de vies, la capacité de pose de bombes, le rayon d'explosion,
 * et l'état de vie/mort du joueur.
 */
public class Player {
    /** Nom du joueur (affichage, score...). */
    private String name;
    /** Ligne actuelle du joueur sur la grille. */
    private int row;
    /** Colonne actuelle du joueur sur la grille. */
    private int col;
    /** Nombre de vies restantes. */
    private int lives;
    /** Nombre maximum de bombes que le joueur peut poser simultanément. */
    private int bombLimit = 1;
    /** Rayon d'explosion des bombes posées par ce joueur. */
    private int bombRange = 1; // Rayon initial à 1, modifiable par bonus
    /** Indique si le joueur est vivant. */
    private boolean alive = true;

    /**
     * Construit un joueur avec nom, position initiale et nombre de vies.
     * @param name Nom du joueur
     * @param row Ligne de départ
     * @param col Colonne de départ
     * @param lives Nombre initial de vies
     */
    public Player(String name, int row, int col, int lives) {
        this.name = name;
        this.row = row;
        this.col = col;
        this.lives = lives;
    }

    /** Retourne la ligne actuelle du joueur. */
    public int getRow() { return row; }
    /** Retourne la colonne actuelle du joueur. */
    public int getCol() { return col; }
    /** Modifie la ligne du joueur. */
    public void setRow(int row) { this.row = row; }
    /** Modifie la colonne du joueur. */
    public void setCol(int col) { this.col = col; }
    /** Retourne le nombre de vies. */
    public int getLives() { return lives; }
    /** Indique si le joueur est vivant. */
    public boolean isAlive() { return alive; }
    /** Retourne le nombre maximum de bombes simultanées. */
    public int getBombLimit() { return bombLimit; }
    /** Retourne le rayon d'explosion des bombes du joueur. */
    public int getBombRange() { return bombRange; }
    /** Retourne le nom du joueur. */
    public String getName() { return name; }

    /**
     * Fait perdre une vie au joueur. Si vies <= 0, il passe à l'état mort.
     */
    public void loseLife() {
        if (lives > 0) lives--;
        if (lives <= 0) alive = false;
    }

    /**
     * Augmente le rayon d'explosion des bombes du joueur (bonus).
     */
    public void increaseBombRange() {
        bombRange++;
    }

    /**
     * Définit explicitement le rayon d'explosion des bombes du joueur.
     * @param range Nouveau rayon
     */
    public void setBombRange(int range) {
        bombRange = range;
    }
}