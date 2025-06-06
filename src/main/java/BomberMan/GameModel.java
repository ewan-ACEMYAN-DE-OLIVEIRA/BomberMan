package BomberMan;

import java.util.Random;

public class GameModel {
    public enum CellType {
        EMPTY,
        WALL,
        DESTRUCTIBLE_WALL,
        PLAYER,
        BOMB,
        EXPLOSION
    }
    
    private static final int GRID_WIDTH = 13;
    private static final int GRID_HEIGHT = 11;
    private final CellType[][] grid = new CellType[GRID_HEIGHT][GRID_WIDTH];
    
    // Joueur
    private int playerRow;
    private int playerCol;
    
    // Bombes
    private int maxBombs = 2;
    private int bombsPlaced = 0;
    
    // Etat du jeu
    private boolean gameRunning = false;
    private int score = 0;
    private String gameStatus = "Prêt à jouer";
    
    public GameModel() {
        resetGrid();
    }
    
    public void resetGrid() {
        // Bordures murs indestructibles
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                if (row == 0 || col == 0 || row == GRID_HEIGHT-1 || col == GRID_WIDTH-1 || (row % 2 == 0 && col % 2 == 0)) {
                    grid[row][col] = CellType.WALL;
                } else {
                    grid[row][col] = CellType.EMPTY;
                }
            }
        }
        // Place joueur en haut à gauche (case vide)
        playerRow = 1;
        playerCol = 1;
        grid[playerRow][playerCol] = CellType.PLAYER;
        // Ajoute des murs destructibles aléatoires (20% de la grille)
        addRandomDestructibleWalls(0.2);
        bombsPlaced = 0;
    }
    
    private boolean isProtectedSpawnZone(int row, int col) {
        // Coins pour éviter les murs devant spawn
        if ((row == 1 && col == 1) || (row == 1 && col == 2) || (row == 2 && col == 1)) return true;
        if ((row == 1 && col == GRID_WIDTH - 2) || (row == 1 && col == GRID_WIDTH - 3) || (row == 2 && col == GRID_WIDTH - 2)) return true;
        if ((row == GRID_HEIGHT - 2 && col == 1) || (row == GRID_HEIGHT - 3 && col == 1) || (row == GRID_HEIGHT - 2 && col == 2)) return true;
        if ((row == GRID_HEIGHT - 2 && col == GRID_WIDTH - 2) || (row == GRID_HEIGHT - 3 && col == GRID_WIDTH - 2) || (row == GRID_HEIGHT - 2 && col == GRID_WIDTH - 3)) return true;
        return false;
    }
    
    public void addRandomDestructibleWalls(double density) {
        Random rand = new Random();
        int placed = 0;
        int max = (int) ((GRID_WIDTH - 2) * (GRID_HEIGHT - 2) * density);
        int tries = 0;
        while (placed < max && tries < 1000) {
            int row = 1 + rand.nextInt(GRID_HEIGHT - 2);
            int col = 1 + rand.nextInt(GRID_WIDTH - 2);
            if (grid[row][col] == CellType.EMPTY && !isProtectedSpawnZone(row, col)) {
                grid[row][col] = CellType.DESTRUCTIBLE_WALL;
                placed++;
            }
            tries++;
        }
    }
    
    public static int getGridWidth() { return GRID_WIDTH; }
    public static int getGridHeight() { return GRID_HEIGHT; }
    
    public CellType getCellType(int row, int col) {
        if (!isValidPosition(row, col)) return null;
        return grid[row][col];
    }
    
    public void setCellType(int row, int col, CellType type) {
        if (isValidPosition(row, col)) grid[row][col] = type;
    }
    
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < GRID_HEIGHT && col >= 0 && col < GRID_WIDTH;
    }
    
    // --- Joueur ---
    public int getPlayerRow() { return playerRow; }
    public int getPlayerCol() { return playerCol; }
    
    public void placePlayer(int row, int col) {
        if (!isValidPosition(row, col)) return;
        if (grid[row][col] == CellType.EMPTY) {
            grid[playerRow][playerCol] = CellType.EMPTY;
            grid[row][col] = CellType.PLAYER;
            playerRow = row;
            playerCol = col;
        }
    }
    
    public boolean canMoveTo(int row, int col) {
        if (!isValidPosition(row, col)) return false;
        // Le joueur NE PEUT PAS traverser une bombe
        return grid[row][col] == CellType.EMPTY;
    }
    
    public boolean movePlayer(int dRow, int dCol) {
        int newRow = playerRow + dRow;
        int newCol = playerCol + dCol;
        if (!isValidPosition(newRow, newCol)) return false;
        if (canMoveTo(newRow, newCol)) {
            if (grid[playerRow][playerCol] == CellType.PLAYER) {
                grid[playerRow][playerCol] = CellType.EMPTY;
            }
            grid[newRow][newCol] = CellType.PLAYER;
            playerRow = newRow;
            playerCol = newCol;
            return true;
        }
        return false;
    }
    
    // --- Bombes ---
    public int getMaxBombs() { return maxBombs; }
    public int getBombsPlaced() { return bombsPlaced; }
    public boolean canPlaceBomb() { return bombsPlaced < maxBombs; }
    
    public boolean placeBomb(int row, int col) {
        // On ne peut poser une bombe que sur la case du joueur, et uniquement si pas déjà une bombe ici
        if (isValidPosition(row, col) && grid[row][col] == CellType.PLAYER && canPlaceBomb()) {
            grid[row][col] = CellType.BOMB;
            bombsPlaced++;
            return true;
        }
        return false;
    }
    
    public void bombExploded() {
        if (bombsPlaced > 0) bombsPlaced--;
    }
    
    // --- Etat du jeu ---
    public boolean isGameRunning() { return gameRunning; }
    public void setGameRunning(boolean running) { this.gameRunning = running; }
    public int getScore() { return score; }
    public void setScore(int s) { this.score = s; }
    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String status) { this.gameStatus = status; }
}