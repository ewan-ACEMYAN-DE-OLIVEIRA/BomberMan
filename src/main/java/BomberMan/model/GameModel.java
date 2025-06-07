package BomberMan.model;

import java.util.Random;

public class GameModel {
    private int aiRow;
    private int aiCol;

    public int getAIRow() {
        return aiRow;
    }
    public int getAICol() {
        return aiCol;
    }

    public enum CellType {
        EMPTY,
        WALL,
        DESTRUCTIBLE_WALL,
        PLAYER1,
        PLAYER2,
        BOMB1,
        BOMB2,
        EXPLOSION,
        BONUS_RANGE,
        MALUS_RANGE,
        AI
    }
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    private Direction player1Direction = Direction.DOWN;
    private Direction player2Direction = Direction.DOWN;

    public Direction getPlayer1Direction() { return player1Direction; }
    public Direction getPlayer2Direction() { return player2Direction; }
    public void setPlayer1Direction(Direction dir) { player1Direction = dir; }
    public void setPlayer2Direction(Direction dir) { player2Direction = dir; }

    private static final int GRID_WIDTH = 13;
    private static final int GRID_HEIGHT = 11;
    private final CellType[][] grid = new CellType[GRID_HEIGHT][GRID_WIDTH];

    // Joueurs
    private int player1Row, player1Col;
    private int player2Row, player2Col;
    private boolean player1Alive = true;
    private boolean player2Alive = true;

    // Bombes
    private int maxBombs = 2;
    private int bombsPlaced1 = 0;
    private int bombsPlaced2 = 0;

    // Portée des bombes (bonus/malus)
    private int bombRange1 = 1;
    private int bombRange2 = 1;
    private final int MAX_BOMB_RANGE = 10;
    private final int MIN_BOMB_RANGE = 1;

    // Etat du jeu
    private boolean gameRunning = false;
    private int score = 0;
    private String gameStatus = "Prêt à jouer";

    // Mémorise la présence d'une bombe sous le joueur
    private boolean bombUnderPlayer1 = false;
    private boolean bombUnderPlayer2 = false;

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
        player1Row = 1;
        player1Col = 1;
        player2Row = GRID_HEIGHT - 2;
        player2Col = GRID_WIDTH - 2;
        grid[player1Row][player1Col] = CellType.PLAYER1;
        grid[player2Row][player2Col] = CellType.PLAYER2;
        player1Alive = true;
        player2Alive = true;
        addRandomDestructibleWalls(0.2);
        addBonusAndMalus();
        bombsPlaced1 = 0;
        bombsPlaced2 = 0;
        bombRange1 = 1;
        bombRange2 = 1;
        bombUnderPlayer1 = false;
        bombUnderPlayer2 = false;
        player1Direction = Direction.DOWN;
        player2Direction = Direction.DOWN;
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
    public int getPlayer1Row() { return player1Row; }
    public int getPlayer1Col() { return player1Col; }
    public int getPlayer2Row() { return player2Row; }
    public int getPlayer2Col() { return player2Col; }
    public boolean isPlayer1Alive() { return player1Alive; }
    public boolean isPlayer2Alive() { return player2Alive; }

    public boolean movePlayer1(int dRow, int dCol) {
        if (dRow == -1) player1Direction = Direction.UP;
        else if (dRow == 1) player1Direction = Direction.DOWN;
        else if (dCol == -1) player1Direction = Direction.LEFT;
        else if (dCol == 1) player1Direction = Direction.RIGHT;

        int curRow = player1Row;
        int curCol = player1Col;
        int newRow = curRow + dRow;
        int newCol = curCol + dCol;
        if (!isValidPosition(newRow, newCol)) return false;
        CellType dest = grid[newRow][newCol];

        // Empêcher d'aller sur l'autre joueur
        if (dest == CellType.PLAYER2) return false;

        if (dest == CellType.EMPTY || dest == CellType.BONUS_RANGE || dest == CellType.MALUS_RANGE) {
            if (bombUnderPlayer1) {
                grid[curRow][curCol] = CellType.BOMB1;
                bombUnderPlayer1 = false;
            } else {
                grid[curRow][curCol] = CellType.EMPTY;
            }
            if (dest == CellType.BONUS_RANGE) increaseBombRange1();
            else if (dest == CellType.MALUS_RANGE) decreaseBombRange1();

            grid[newRow][newCol] = CellType.PLAYER1;
            player1Row = newRow;
            player1Col = newCol;
            return true;
        }
        return false;
    }

    public boolean movePlayer2(int dRow, int dCol) {
        if (dRow == -1) player2Direction = Direction.UP;
        else if (dRow == 1) player2Direction = Direction.DOWN;
        else if (dCol == -1) player2Direction = Direction.LEFT;
        else if (dCol == 1) player2Direction = Direction.RIGHT;

        int curRow = player2Row;
        int curCol = player2Col;
        int newRow = curRow + dRow;
        int newCol = curCol + dCol;
        if (!isValidPosition(newRow, newCol)) return false;
        CellType dest = grid[newRow][newCol];

        // Empêcher d'aller sur l'autre joueur
        if (dest == CellType.PLAYER1) return false;

        if (dest == CellType.EMPTY || dest == CellType.BONUS_RANGE || dest == CellType.MALUS_RANGE) {
            if (bombUnderPlayer2) {
                grid[curRow][curCol] = CellType.BOMB2;
                bombUnderPlayer2 = false;
            } else {
                grid[curRow][curCol] = CellType.EMPTY;
            }
            if (dest == CellType.BONUS_RANGE) increaseBombRange2();
            else if (dest == CellType.MALUS_RANGE) decreaseBombRange2();

            grid[newRow][newCol] = CellType.PLAYER2;
            player2Row = newRow;
            player2Col = newCol;
            return true;
        }
        return false;
    }

    // --- Bombes ---
    public int getMaxBombs() { return maxBombs; }
    public int getBombsPlaced1() { return bombsPlaced1; }
    public int getBombsPlaced2() { return bombsPlaced2; }
    public boolean canPlaceBomb1() { return bombsPlaced1 < maxBombs; }
    public boolean canPlaceBomb2() { return bombsPlaced2 < maxBombs; }
    public boolean isBombUnderPlayer1() { return bombUnderPlayer1; }
    public boolean isBombUnderPlayer2() { return bombUnderPlayer2; }

    public boolean placeBombForPlayer(int player) {
        int row = (player == 1) ? player1Row : player2Row;
        int col = (player == 1) ? player1Col : player2Col;
        boolean bombUnderPlayer = (player == 1) ? bombUnderPlayer1 : bombUnderPlayer2;
        if (isValidPosition(row, col)
                && grid[row][col] == (player == 1 ? CellType.PLAYER1 : CellType.PLAYER2)
                && ((player == 1 && canPlaceBomb1() && !bombUnderPlayer1) || (player == 2 && canPlaceBomb2() && !bombUnderPlayer2))
        ) {
            if (player == 1) bombUnderPlayer1 = true;
            else bombUnderPlayer2 = true;
            if (player == 1) bombsPlaced1++;
            else bombsPlaced2++;
            return true;
        }
        return false;
    }

    public void bombExploded(int player) {
        if (player == 1 && bombsPlaced1 > 0) bombsPlaced1--;
        if (player == 2 && bombsPlaced2 > 0) bombsPlaced2--;
    }

    // Portée des bombes
    public int getBombRange1() { return bombRange1; }
    public int getBombRange2() { return bombRange2; }
    public void increaseBombRange1() { if (bombRange1 < MAX_BOMB_RANGE) bombRange1++; }
    public void increaseBombRange2() { if (bombRange2 < MAX_BOMB_RANGE) bombRange2++; }
    public void decreaseBombRange1() { if (bombRange1 > MIN_BOMB_RANGE) bombRange1--; }
    public void decreaseBombRange2() { if (bombRange2 > MIN_BOMB_RANGE) bombRange2--; }

    // --- Explosion logic helper ---
    public void killPlayer(int player) {
        if (player == 1) player1Alive = false;
        if (player == 2) player2Alive = false;
        gameRunning = false;
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
