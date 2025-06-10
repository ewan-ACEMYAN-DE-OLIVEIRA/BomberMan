package BomberMan.model;

public class Bomb {
    private int row, col;
    private int timer = 60;
    private int range;
    private Player owner;

    public Bomb(int row, int col, Player owner) {
        this.row = row;
        this.col = col;
        this.owner = owner;
        this.range = owner.getBombRange(); // Prend le rayon actuel du joueur
    }

    public void tick() { timer--; }
    public int getTimer() { return timer; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getRange() { return range; }
    public Player getOwner() { return owner; }
}