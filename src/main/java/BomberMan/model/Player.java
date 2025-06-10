package BomberMan.model;

public class Player {
    private String name;
    private int row;
    private int col;
    private int lives;
    private int bombLimit = 1;
    private int bombRange = 1; // Rayon initial à 1, modifiable par bonus
    private boolean alive = true;

    public Player(String name, int row, int col, int lives) {
        this.name = name;
        this.row = row;
        this.col = col;
        this.lives = lives;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }
    public int getLives() { return lives; }
    public boolean isAlive() { return alive; }
    public int getBombLimit() { return bombLimit; }
    public int getBombRange() { return bombRange; }
    public String getName() { return name; }

    public void loseLife() {
        if (lives > 0) lives--;
        if (lives <= 0) alive = false;
    }

    // Pour augmenter le rayon via un bonus
    public void increaseBombRange() {
        bombRange++;
    }
    // Pour diminuer ou réinitialiser si besoin
    public void setBombRange(int range) {
        bombRange = range;
    }
}