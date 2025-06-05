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
    
    // Compteur de bombes
    private int bombCount = 10; // Max bombes posables
    private int bombsPlaced = 0;
    
    // Etat du jeu
    private boolean gameRunning = false;
    private int score = 0;
    private String gameStatus = "Prêt à jouer";
    private boolean playerAlive = true;
    
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
        
        // Réinitialise les bombes
        bombsPlaced = 0;
        bombCount = 10;
        playerAlive = true;
        setGameRunning(false);
    }








    private boolean isProtectedSpawnZone(int row, int col) {
        // Coin haut gauche (spawn joueur 1)
        if ((row == 1 && col == 1) || (row == 1 && col == 2) || (row == 2 && col == 1)) return true;
        // Coin haut droite (spawn joueur 2)
        if ((row == 1 && col == GRID_WIDTH - 2) || (row == 1 && col == GRID_WIDTH - 3) || (row == 2 && col == GRID_WIDTH - 2)) return true;
        // Coin bas gauche (spawn joueur 3)
        if ((row == GRID_HEIGHT - 2 && col == 1) || (row == GRID_HEIGHT - 3 && col == 1) || (row == GRID_HEIGHT - 2 && col == 2)) return true;
        // Coin bas droite (spawn joueur 4)
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
            // Ne pas placer sur les cases protégées
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
        return grid[row][col] == CellType.EMPTY;
    }
    
    public boolean movePlayer(int dRow, int dCol) {
        int newRow = playerRow + dRow;
        int newCol = playerCol + dCol;
        if (canMoveTo(newRow, newCol)) {
            grid[playerRow][playerCol] = CellType.EMPTY;
            grid[newRow][newCol] = CellType.PLAYER;
            playerRow = newRow;
            playerCol = newCol;
            return true;
        }
        return false;
    }
    
    // --- Bombe ---
    public boolean canPlaceBomb() {
        return bombsPlaced < bombCount;
    }
    public boolean placeBomb(int row, int col) {
        if (isValidPosition(row, col) && (grid[row][col] == CellType.EMPTY || grid[row][col] == CellType.PLAYER) && canPlaceBomb()) {
            grid[row][col] = CellType.BOMB;
            bombsPlaced++;
            return true;
        }
        return false;
    }
    public void bombExploded() {
        if (bombsPlaced > 0) bombsPlaced--;
    }
    public int getBombCount() { return bombCount; }
    public void setBombCount(int n) { bombCount = Math.max(1, n); }
    public int getBombsPlaced() { return bombsPlaced; }
    
    // --- Etat du jeu ---
    public boolean isGameRunning() { return gameRunning; }
    public void setGameRunning(boolean running) { this.gameRunning = running; }
    public int getScore() { return score; }
    public void setScore(int s) { this.score = s; }
    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String status) { this.gameStatus = status; }
    public boolean isPlayerAlive() { return playerAlive; }
    public void setPlayerAlive(boolean alive) { this.playerAlive = alive; }
}