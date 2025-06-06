package BomberMan;

import java.util.Random;

public class GameModel {
    private int aiRow;
    private int aiCol;

    public int getAIRow() { return aiRow; }
    public int getAICol() { return aiCol; }

    public enum CellType {
        EMPTY, WALL, DESTRUCTIBLE_WALL, PLAYER, BOMB, EXPLOSION, BONUS_RANGE, MALUS_RANGE, AI
    }
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    private Direction playerDirection = Direction.DOWN;

    public Direction getPlayerDirection() { return playerDirection; }
    public void setPlayerDirection(Direction dir) { playerDirection = dir; }

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
    private static final int MAX_BOMB_RANGE = 10;
    private static final int MIN_BOMB_RANGE = 1;

    // Etat du jeu
    private boolean gameRunning = false;
    private int score = 0;
    private String gameStatus = "Prêt à jouer";

    // Mémorise la présence d'une bombe sous le joueur
    private boolean bombUnderPlayer = false;

    public GameModel() {
        resetGrid();
    }

    public void resetGrid() {
        // Génération de la grille (murs fixes)
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                if (row == 0 || col == 0 || row == GRID_HEIGHT - 1 || col == GRID_WIDTH - 1 || (row % 2 == 0 && col % 2 == 0)) {
                    grid[row][col] = CellType.WALL;
                } else {
                    grid[row][col] = CellType.EMPTY;
                }
            }
        }
        // Placement IA
        aiRow = GRID_HEIGHT - 2;
        aiCol = GRID_WIDTH - 2;
        if (grid[aiRow][aiCol] == CellType.EMPTY) {
            grid[aiRow][aiCol] = CellType.AI;
        }

        // Placement joueur
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
        // Zones protégées pour éviter de bloquer le spawn joueur/IA
        return
                (row == 1 && col == 1) || (row == 1 && col == 2) || (row == 2 && col == 1) ||
                        (row == 1 && col == GRID_WIDTH - 2) || (row == 1 && col == GRID_WIDTH - 3) || (row == 2 && col == GRID_WIDTH - 2) ||
                        (row == GRID_HEIGHT - 2 && col == 1) || (row == GRID_HEIGHT - 3 && col == 1) || (row == GRID_HEIGHT - 2 && col == 2) ||
                        (row == GRID_HEIGHT - 2 && col == GRID_WIDTH - 2) || (row == GRID_HEIGHT - 3 && col == GRID_WIDTH - 2) || (row == GRID_HEIGHT - 2 && col == GRID_WIDTH - 3);
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
        addRandomItem(CellType.BONUS_RANGE, 3);
        addRandomItem(CellType.MALUS_RANGE, 3);
    }

    private void addRandomItem(CellType type, int count) {
        Random rand = new Random();
        int placed = 0;
        while (placed < count) {
            int row = 1 + rand.nextInt(GRID_HEIGHT - 2);
            int col = 1 + rand.nextInt(GRID_WIDTH - 2);
            if (grid[row][col] == CellType.EMPTY && !isProtectedSpawnZone(row, col)) {
                grid[row][col] = type;
                placed++;
            }
        }
    }

    public static int getGridWidth() { return GRID_WIDTH; }
    public static int getGridHeight() { return GRID_HEIGHT; }

    public CellType getCellType(int row, int col) {
        if (!isValidPosition(row, col)) { return null; }
        return grid[row][col];
    }

    public void setCellType(int row, int col, CellType type) {
        if (isValidPosition(row, col)) { grid[row][col] = type; }
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
        return grid[row][col] == CellType.EMPTY ||
                grid[row][col] == CellType.BONUS_RANGE ||
                grid[row][col] == CellType.MALUS_RANGE;
    }

    public boolean movePlayer(int dRow, int dCol) {
        if      (dRow == -1) playerDirection = Direction.UP;
        else if (dRow ==  1) playerDirection = Direction.DOWN;
        else if (dCol == -1) playerDirection = Direction.LEFT;
        else if (dCol ==  1) playerDirection = Direction.RIGHT;

        int newRow = playerRow + dRow;
        int newCol = playerCol + dCol;
        if (!isValidPosition(newRow, newCol)) return false;
        if (canMoveTo(newRow, newCol)) {
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
        if (bombsPlaced > 0) { bombsPlaced--; }
    }

    // Gestion de la portée des bombes
    public int getBombRange() { return bombRange; }
    public void increaseBombRange() {
        if (bombRange < MAX_BOMB_RANGE) { bombRange++; }
    }
    public void decreaseBombRange() {
        if (bombRange > MIN_BOMB_RANGE) { bombRange--; }
    }

    // --- IA ---
    public void placeAI(int row, int col) {
        if (!isValidPosition(row, col)) return;
        if (grid[row][col] == CellType.EMPTY) {
            grid[aiRow][aiCol] = CellType.EMPTY;
            grid[row][col] = CellType.AI;
            aiRow = row;
            aiCol = col;
        }
    }

    public boolean moveAI(int dRow, int dCol) {
        int newRow = aiRow + dRow;
        int newCol = aiCol + dCol;
        if (!isValidPosition(newRow, newCol)) return false;
        CellType target = grid[newRow][newCol];
        if (target == CellType.EMPTY || target == CellType.BONUS_RANGE || target == CellType.MALUS_RANGE) {
            grid[aiRow][aiCol] = CellType.EMPTY;
            grid[newRow][newCol] = CellType.AI;
            aiRow = newRow;
            aiCol = newCol;
            return true;
        }
        return false;
    }

    // --- Etat du jeu ---
    public boolean isGameRunning() { return gameRunning; }
    public void setGameRunning(boolean running) { this.gameRunning = running; }
    public int getScore() { return score; }
    public void setScore(int s) { this.score = s; }
    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String status) { this.gameStatus = status; }
}