package BomberMan;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;
import javafx.util.Duration;

public class GameViewController {
    @FXML
    private GridPane gameGrid;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label timerLabel;
    @FXML
    private Button startButton;
    @FXML
    private Button pauseButton;
    
    private Timeline timer;
    private int elapsedSeconds = 0;
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
        pauseButton.setDisable(true);
        javafx.application.Platform.runLater(() -> handleStartGame());
    }
    
    private void startTimer() {
        if (timer != null) timer.stop();
        elapsedSeconds = 0;
        timerLabel.setText("Time: 00:00");
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            elapsedSeconds++;
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
    private void pauseTimer() {
        if (timer != null) timer.pause();
    }
    
    private void resumeTimer() {
        if (timer != null) timer.play();
    }
    
    private void stopTimer() {
        if (timer != null) timer.stop();
        timerLabel.setText("Time: 00:00");
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
            boolean moved = false;
            if (!gameModel.isGameRunning() || gameController.isPaused()) return;
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
        } else if (type == GameModel.CellType.BONUS_RANGE) {
            messageLabel.setText("Bonus : +1 portée de bombe !");
        } else if (type == GameModel.CellType.MALUS_RANGE) {
            messageLabel.setText("Malus : -1 portée de bombe !");
        }
    }
    
    private void handlePlaceBomb() {
        if (gameController.isPaused()) return;
        int row = gameModel.getPlayerRow();
        int col = gameModel.getPlayerCol();
        if (!gameModel.canPlaceBomb()) {
            messageLabel.setText("Nombre maximum de bombes atteint !");
            return;
        }
        if (gameModel.placeBomb(row, col)) {
            updateGridDisplay();
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
        int range = gameModel.getBombRange();
        boolean playerWasOnBomb = (gameModel.getPlayerRow() == row && gameModel.getPlayerCol() == col && gameModel.isBombUnderPlayer());
        
        // Si le joueur est sur la bombe, on laisse PLAYER sur la case mais retire le flag bombUnderPlayer
        if (playerWasOnBomb) {
            // Affiche l'explosion mais garde PLAYER pour l'affichage
            gameModel.setCellType(row, col, GameModel.CellType.EXPLOSION);
        } else {
            gameModel.setCellType(row, col, GameModel.CellType.EXPLOSION);
        }
        
        int[][] dirs = { {-1,0}, {1,0}, {0,-1}, {0,1} };
        for (int[] d : dirs) {
            for (int dist = 1; dist <= range; dist++) {
                int r = row + d[0] * dist, c = col + d[1] * dist;
                if (!gameModel.isValidPosition(r, c)) break;
                GameModel.CellType t = gameModel.getCellType(r, c);
                if (t == GameModel.CellType.WALL) {
                    break;
                } else if (t == GameModel.CellType.DESTRUCTIBLE_WALL) {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                    break;
                } else if (t == GameModel.CellType.BOMB) {
                    explodeBomb(r, c);
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                } else if (t == GameModel.CellType.PLAYER) {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                    endGame();
                } else if (t == GameModel.CellType.BONUS_RANGE || t == GameModel.CellType.MALUS_RANGE) {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                } else {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                }
            }
        }
        
        updateGridDisplay();
        gameModel.bombExploded();
        
        // Nettoyage explosion après 400ms
        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                // Nettoie centre
                if (gameModel.getCellType(row, col) == GameModel.CellType.EXPLOSION) {
                    // Si le joueur est resté sur place, on remet PLAYER sinon EMPTY
                    if (playerWasOnBomb && gameModel.getPlayerRow() == row && gameModel.getPlayerCol() == col) {
                        gameModel.setCellType(row, col, GameModel.CellType.PLAYER);
                    } else {
                        gameModel.setCellType(row, col, GameModel.CellType.EMPTY);
                    }
                }
                for (int[] d : dirs) {
                    for (int dist = 1; dist <= range; dist++) {
                        int r = row + d[0] * dist, c = col + d[1] * dist;
                        if (gameModel.isValidPosition(r, c)) {
                            if (gameModel.getCellType(r, c) == GameModel.CellType.EXPLOSION) {
                                gameModel.setCellType(r, c, GameModel.CellType.EMPTY);
                            }
                        }
                    }
                }
                updateGridDisplay();
            });
        }).start();
    }
    
    private void endGame() {
        gameModel.setGameRunning(false);
        gameModel.setGameStatus("Game Over !");
        statusLabel.setText(gameModel.getGameStatus());
        messageLabel.setText("Le joueur a été touché ! Appuyez sur Reset pour recommencer.");
        updateGridDisplay();
    }
    
    private void updateGridDisplay() {
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof StackPane cell) {
                Integer col = GridPane.getColumnIndex(node);
                Integer row = GridPane.getRowIndex(node);
                if (col == null || row == null) continue;
                
                cell.getStyleClass().removeAll(
                        "cell-empty", "cell-wall", "cell-destructible", "cell-player", "cell-bomb",
                        "cell-explosion", "cell-bonus-range", "cell-malus-range"
                );
                cell.getChildren().clear();
                
                boolean hasPlayer = (gameModel.getPlayerRow() == row && gameModel.getPlayerCol() == col);
                boolean hasBomb = (gameModel.getCellType(row, col) == GameModel.CellType.BOMB)
                        || (hasPlayer && gameModel.isBombUnderPlayer());
                
                if (hasPlayer && hasBomb) {
                    cell.getStyleClass().addAll("cell-bomb", "cell-player");
                    try {
                        String imagePath = "/com/example/BomberMan/Personnages/Blanc/Face.png";
                        Image playerImage = new Image(getClass().getResourceAsStream(imagePath));
                        ImageView imageView = new ImageView(playerImage);
                        imageView.setFitWidth(28);
                        imageView.setFitHeight(28);
                        cell.getChildren().add(imageView);
                    } catch (Exception e) {}
                } else if (hasPlayer) {
                    cell.getStyleClass().add("cell-player");
                    try {
                        String imagePath = "/com/example/BomberMan/Personnages/Blanc/Face.png";
                        Image playerImage = new Image(getClass().getResourceAsStream(imagePath));
                        ImageView imageView = new ImageView(playerImage);
                        imageView.setFitWidth(28);
                        imageView.setFitHeight(28);
                        cell.getChildren().add(imageView);
                    } catch (Exception e) {}
                } else {
                    switch (gameModel.getCellType(row, col)) {
                        case WALL -> cell.getStyleClass().add("cell-wall");
                        case DESTRUCTIBLE_WALL -> cell.getStyleClass().add("cell-destructible");
                        case BOMB -> cell.getStyleClass().add("cell-bomb");
                        case EXPLOSION -> cell.getStyleClass().add("cell-explosion");
                        case BONUS_RANGE -> cell.getStyleClass().add("cell-bonus-range");
                        case MALUS_RANGE -> cell.getStyleClass().add("cell-malus-range");
                        default -> cell.getStyleClass().add("cell-empty");
                    }
                }
            }
        }
        scoreLabel.setText(String.valueOf(gameModel.getScore()));
        statusLabel.setText(gameModel.getGameStatus());
    }
    
    @FXML
    private void handleStartGame() {
        gameController.startGame();
        updateGridDisplay();
        messageLabel.setText("Partie démarrée !");
        startTimer();
        gameGrid.requestFocus();
        startButton.setDisable(true);
        pauseButton.setDisable(false);
    }
    
    @FXML
    private void handlePauseGame() {
        gameController.pauseGame();
        statusLabel.setText(gameModel.getGameStatus());
        if (gameController.isPaused()) {
            messageLabel.setText("Jeu en pause");
            pauseTimer();
            pauseButton.setText("Reprendre");
        } else {
            messageLabel.setText("Jeu relancé !");
            resumeTimer();
            pauseButton.setText("Pause");
        }
        gameGrid.requestFocus();
    }
    
    @FXML
    private void handleResetGame() {
        gameController.resetGame();
        updateGridDisplay();
        messageLabel.setText("Jeu réinitialisé !");
        gameGrid.requestFocus();
        startButton.setDisable(false);
        pauseButton.setText("Pause");
        pauseButton.setDisable(true);
        timerLabel.setText("Time: 00:00");
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
                    case BONUS_RANGE -> sb.append("+ ");
                    case MALUS_RANGE -> sb.append("- ");
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