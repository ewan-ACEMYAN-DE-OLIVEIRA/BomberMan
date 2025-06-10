package BomberMan.model;

public class Explosion {
    private int row, col;
    private int timer = 30;

    public Explosion(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getTimer() { return timer; }
    public void tick() { timer--; }
}
