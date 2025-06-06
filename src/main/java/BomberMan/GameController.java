package BomberMan;

public class GameController {
    private final GameModel gameModel;
    private boolean isPaused = false;
    
    public GameController(GameModel gameModel) {
        this.gameModel = gameModel;
        initializeController();
    }
    
    private void initializeController() {
        gameModel.setGameStatus("Contrôleur initialisé");
    }
    
    public void startGame() {
        if (!gameModel.isGameRunning() && !isPaused) {
            gameModel.resetGrid();
        }
        gameModel.setGameRunning(true);
        isPaused = false;
        gameModel.setGameStatus("Jeu en cours");
    }
    
    public void pauseGame() {
        if (gameModel.isGameRunning()) {
            isPaused = !isPaused;
            if (isPaused) {
                gameModel.setGameStatus("Jeu en pause");
            } else {
                gameModel.setGameStatus("Jeu en cours");
            }
        }
    }
    
    public void resetGame() {
        stopGame();
        gameModel.resetGrid();
        gameModel.setScore(0);
        gameModel.setGameStatus("Jeu réinitialisé");
    }
    
    public void stopGame() {
        gameModel.setGameRunning(false);
        isPaused = false;
        gameModel.setGameStatus("Jeu arrêté");
    }
    public boolean destroyWall(int row, int col) {
        // Ne pas détruire un mur si le joueur est dessus
        if (gameModel.getCellType(row, col) == GameModel.CellType.DESTRUCTIBLE_WALL
                && !(gameModel.getPlayerRow() == row && gameModel.getPlayerCol() == col)) {
            gameModel.setCellType(row, col, GameModel.CellType.EMPTY);
            gameModel.setScore(gameModel.getScore() + 10);
            return true;
        }
        return false;
    }
    public void addRandomDestructibleWalls(double density) {
        gameModel.addRandomDestructibleWalls(density);
        gameModel.setGameStatus("Murs destructibles ajoutés !");
    }
    
    public boolean isPaused() {
        return isPaused;
    }
}