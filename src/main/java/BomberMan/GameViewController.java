package BomberMan;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;

public class GameViewController {
    @FXML private GridPane gameGrid;
    @FXML private Label scoreLabel;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;
    @FXML private Label bombLabel; // Ajoute ce label dans ton FXML (ex: fx:id="bombLabel")
    
    private GameModel gameModel;
    private GameController gameController;
    
    @FXML
    public void initialize() {
        gameModel = new GameModel();
        gameController = new GameController(gameModel);
        setupGridDisplay();
        updateGridDisplay();
        setupKeyHandlers();
        statusLabel.setText(gameModel.getGameStatus());
        scoreLabel.setText("0");
        updateBombLabel();
    }
    
    private void setupGridDisplay() {
        gameGrid.getChildren().clear();
        for (int row = 0; row < GameModel.getGridHeight(); row++) {
            for (int col = 0; col < GameModel.getGridWidth(); col++) {
                StackPane cell = new StackPane();
                cell.setMinSize(32, 32);
                cell.setMaxSize(32, 32);
                cell.getStyleClass().add("game-cell");
                int finalRow = row, finalCol = col;
                cell.setOnMouseClicked(e -> handleCellClick(finalRow, finalCol));
                gameGrid.add(cell, col, row);
            }
        }
    }
    
    private void setupKeyHandlers() {
        gameGrid.setFocusTraversable(true);
        gameGrid.setOnKeyPressed(event -> {
            if (!gameModel.isGameRunning() || !gameModel.isPlayerAlive()) return;
            boolean moved = false;
            if (event.getCode() == KeyCode.Z) moved = gameModel.movePlayer(-1, 0); // haut
            if (event.getCode() == KeyCode.S) moved = gameModel.movePlayer(1, 0);  // bas
            if (event.getCode() == KeyCode.Q) moved = gameModel.movePlayer(0, -1); // gauche
            if (event.getCode() == KeyCode.D) moved = gameModel.movePlayer(0, 1);  // droite
            if (event.getCode() == KeyCode.SPACE) handlePlaceBomb();
            if (moved) updateGridDisplay();
        });
    }
    
    private void handleCellClick(int row, int col) {
        GameModel.CellType type = gameModel.getCellType(row, col);
        if (type == GameModel.CellType.DESTRUCTIBLE_WALL) {
            boolean destroyed = gameController.destroyWall(row, col);
            if (destroyed) {
                messageLabel.setText("Mur destructible détruit !");
                updateGridDisplay();
            }
        } else if (type == GameModel.CellType.EMPTY) {
            messageLabel.setText("Case vide");
        } else if (type == GameModel.CellType.WALL) {
            messageLabel.setText("Mur indestructible");
        } else if (type == GameModel.CellType.PLAYER) {
            messageLabel.setText("Le joueur est ici !");
        }
    }
    
    private void handlePlaceBomb() {
        if (!gameModel.canPlaceBomb()) {
            messageLabel.setText("Aucune bombe disponible !");
            return;
        }
        int row = gameModel.getPlayerRow();
        int col = gameModel.getPlayerCol();
        if (gameModel.placeBomb(row, col)) {
            updateGridDisplay();
            updateBombLabel();
            // Lance un timer pour explosion
            new Thread(() -> {
                try {
                    Thread.sleep(1500); // 1.5 seconde avant explosion
                } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    explodeBomb(row, col);
                });
            }).start();
        }
    }
    
    private void explodeBomb(int row, int col) {
        // Bombe explose et détruit les murs destructibles dans les 4 directions (1 case)
        gameModel.setCellType(row, col, GameModel.CellType.EXPLOSION);
        int[][] dirs = { {0,0}, {-1,0}, {1,0}, {0,-1}, {0,1} };
        boolean playerHit = false;
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            if (gameModel.isValidPosition(r, c)) {
                GameModel.CellType t = gameModel.getCellType(r, c);
                if (t == GameModel.CellType.DESTRUCTIBLE_WALL) {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                } else if (t == GameModel.CellType.PLAYER) {
                    playerHit = true;
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                } else if (t == GameModel.CellType.BOMB) {
                    // Enchaînement des explosions (optionnel)
                    explodeBomb(r, c);
                }
            }
        }
        updateGridDisplay();
        gameModel.bombExploded();
        updateBombLabel();
        
        if (playerHit) {
            endGame();
            return;
        }
        
        // Affiche l'explosion pendant 400ms puis repasse en EMPTY
        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                for (int[] d : dirs) {
                    int r = row + d[0], c = col + d[1];
                    if (gameModel.isValidPosition(r, c)) {
                        if (gameModel.getCellType(r, c) == GameModel.CellType.EXPLOSION) {
                            gameModel.setCellType(r, c, GameModel.CellType.EMPTY);
                        }
                    }
                }
                updateGridDisplay();
            });
        }).start();
    }
    
    private void endGame() {
        gameModel.setPlayerAlive(false);
        gameModel.setGameRunning(false);
        gameModel.setGameStatus("Game Over !");
        statusLabel.setText(gameModel.getGameStatus());
        messageLabel.setText("Le joueur a été touché ! Appuyez sur Reset pour recommencer.");
        updateGridDisplay();
    }
    
    private void updateGridDisplay() {
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof StackPane) {
                Integer col = GridPane.getColumnIndex(node);
                Integer row = GridPane.getRowIndex(node);
                if (col == null || row == null) continue;
                GameModel.CellType type = gameModel.getCellType(row, col);
                node.getStyleClass().removeAll("cell-empty", "cell-wall", "cell-destructible", "cell-player", "cell-bomb", "cell-explosion");
                switch (type) {
                    case WALL -> node.getStyleClass().add("cell-wall");
                    case DESTRUCTIBLE_WALL -> node.getStyleClass().add("cell-destructible");
                    case PLAYER -> node.getStyleClass().add("cell-player");
                    case BOMB -> node.getStyleClass().add("cell-bomb");
                    case EXPLOSION -> node.getStyleClass().add("cell-explosion");
                    default -> node.getStyleClass().add("cell-empty");
                }
            }
        }
        scoreLabel.setText(String.valueOf(gameModel.getScore()));
        statusLabel.setText(gameModel.getGameStatus());
    }
    
    private void updateBombLabel() {
        if (bombLabel != null) {
            bombLabel.setText("Bombes : " + (gameModel.getBombCount() - gameModel.getBombsPlaced()) + " / " + gameModel.getBombCount());
        }
    }
    
    // Boutons UI
    
    @FXML
    private void handleStartGame() {
        gameController.startGame();
        updateGridDisplay();
        updateBombLabel();
        messageLabel.setText("Partie démarrée !");
        gameGrid.requestFocus();
    }
    
    @FXML
    private void handlePauseGame() {
        gameController.pauseGame();
        statusLabel.setText(gameModel.getGameStatus());
        messageLabel.setText("Pause/Relance");
        gameGrid.requestFocus();
    }
    
    @FXML
    private void handleResetGame() {
        gameController.resetGame();
        updateGridDisplay();
        updateBombLabel();
        messageLabel.setText("Jeu réinitialisé !");
        gameGrid.requestFocus();
    }
    
    @FXML
    private void handleAddWalls() {
        gameController.addRandomDestructibleWalls(0.2);
        updateGridDisplay();
        messageLabel.setText("Murs destructibles ajoutés !");
        gameGrid.requestFocus();
    }
    
    @FXML
    private void handleDebugGrid() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < GameModel.getGridHeight(); row++) {
            for (int col = 0; col < GameModel.getGridWidth(); col++) {
                switch (gameModel.getCellType(row, col)) {
                    case WALL -> sb.append("# ");
                    case DESTRUCTIBLE_WALL -> sb.append("D ");
                    case PLAYER -> sb.append("P ");
                    case BOMB -> sb.append("B ");
                    case EXPLOSION -> sb.append("X ");
                    default -> sb.append(". ");
                }
            }
            sb.append("\n");
        }
        System.out.println(sb);
        messageLabel.setText("Grille affichée dans la console");
        gameGrid.requestFocus();
    }
}