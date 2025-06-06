package BomberMan;

import java.util.Random;

public class GameModel {
    public enum CellType {
        EMPTY,
        WALL,
        DESTRUCTIBLE_WALL,
        PLAYER,
        BOMB,
        EXPLOSION,
        BONUS_RANGE,
        MALUS_RANGE
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
    
    // Portée des bombes (bonus/malus)
    private int bombRange = 1;
    private final int MAX_BOMB_RANGE = 10;
    private final int MIN_BOMB_RANGE = 1;
    
    // Etat du jeu
    private boolean gameRunning = false;
    private int score = 0;
    private String gameStatus = "Prêt à jouer";
    
    // Pour mémoriser la présence d'une bombe sous le joueur
    private boolean bombUnderPlayer = false;
    
    public GameModel() {
        resetGrid();
    }
    
    public void resetGrid() {
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                if (row == 0 || col == 0 || row == GRID_HEIGHT-1 || col == GRID_WIDTH-1 || (row % 2 == 0 && col % 2 == 0)) {
                    grid[row][col] = CellType.WALL;
                } else {
                    grid[row][col] = CellType.EMPTY;
                }
            }
        }
        playerRow = 1;
        playerCol = 1;
        grid[playerRow][playerCol] = CellType.PLAYER;
        addRandomDestructibleWalls(0.2);
        addBonusAndMalus();
        bombsPlaced = 0;
        bombRange = 1;
        bombUnderPlayer = false;
    }
    
    private boolean isProtectedSpawnZone(int row, int col) {
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
    
    public void addBonusAndMalus() {
        Random rand = new Random();
        for(int i=0; i<3; i++) {
            int row, col;
            do {
                row = 1 + rand.nextInt(GRID_HEIGHT - 2);
                col = 1 + rand.nextInt(GRID_WIDTH - 2);
            } while (grid[row][col] != CellType.EMPTY || isProtectedSpawnZone(row, col));
            grid[row][col] = CellType.BONUS_RANGE;
        }
        for(int i=0; i<3; i++) {
            int row, col;
            do {
                row = 1 + rand.nextInt(GRID_HEIGHT - 2);
                col = 1 + rand.nextInt(GRID_WIDTH - 2);
            } while (grid[row][col] != CellType.EMPTY || isProtectedSpawnZone(row, col));
            grid[row][col] = CellType.MALUS_RANGE;
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
        if (grid[row][col] == CellType.EMPTY || grid[row][col] == CellType.BONUS_RANGE || grid[row][col] == CellType.MALUS_RANGE) {
            // Gestion de la bombe sous le joueur
            if (bombUnderPlayer) {
                grid[playerRow][playerCol] = CellType.BOMB;
                bombUnderPlayer = false;
            } else {
                grid[playerRow][playerCol] = CellType.EMPTY;
            }
            if (grid[row][col] == CellType.BONUS_RANGE) {
                increaseBombRange();
            } else if (grid[row][col] == CellType.MALUS_RANGE) {
                decreaseBombRange();
            }
            grid[row][col] = CellType.PLAYER;
            playerRow = row;
            playerCol = col;
        }
    }
    
    public boolean canMoveTo(int row, int col) {
        if (!isValidPosition(row, col)) return false;
        // On autorise à marcher sur EMPTY, BONUS_RANGE, MALUS_RANGE
        return grid[row][col] == CellType.EMPTY ||
                grid[row][col] == CellType.BONUS_RANGE ||
                grid[row][col] == CellType.MALUS_RANGE;
    }
    
    public boolean movePlayer(int dRow, int dCol) {
        int newRow = playerRow + dRow;
        int newCol = playerCol + dCol;
        if (!isValidPosition(newRow, newCol)) return false;
        if (canMoveTo(newRow, newCol)) {
            // Gestion bombe sous le joueur
            if (bombUnderPlayer) {
                grid[playerRow][playerCol] = CellType.BOMB;
                bombUnderPlayer = false;
            } else {
                grid[playerRow][playerCol] = CellType.EMPTY;
            }
            if (grid[newRow][newCol] == CellType.BONUS_RANGE) {
                increaseBombRange();
            } else if (grid[newRow][newCol] == CellType.MALUS_RANGE) {
                decreaseBombRange();
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
    
    public boolean isBombUnderPlayer() { return bombUnderPlayer; }
    
    public boolean placeBomb(int row, int col) {
        if (isValidPosition(row, col) && grid[row][col] == CellType.PLAYER && canPlaceBomb() && !bombUnderPlayer) {
            bombUnderPlayer = true;
            bombsPlaced++;
            return true;
        }
        return false;
    }
    
    public void bombExploded() {
        if (bombsPlaced > 0) bombsPlaced--;
    }
    
    // Gestion de la portée des bombes
    public int getBombRange() { return bombRange; }
    public void increaseBombRange() {
        if (bombRange < MAX_BOMB_RANGE) bombRange++;
    }
    public void decreaseBombRange() {
        if (bombRange > MIN_BOMB_RANGE) bombRange--;
    }
    
    // --- Etat du jeu ---
    public boolean isGameRunning() { return gameRunning; }
    public void setGameRunning(boolean running) { this.gameRunning = running; }
    public int getScore() { return score; }
    public void setScore(int s) { this.score = s; }
    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String status) { this.gameStatus = status; }
}