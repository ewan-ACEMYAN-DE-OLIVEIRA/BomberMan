package BomberMan;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;
import javafx.stage.Stage;
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
    @FXML
    private Button menuButton;
    
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
    @FXML
    private void handleReturnToMenu() {
        try {
            // Charge le menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/BomberMan/MenuView.fxml"));
            Parent menuRoot = loader.load();
            
            // Remplace la scène actuelle par le menu
            Stage stage = (Stage) menuButton.getScene().getWindow();
            Scene scene = new Scene(menuRoot, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("Menu BomberMan");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            
            switch (event.getCode()) {
                // Joueur 1
                case Z -> {
                    gameModel.setPlayer1Direction(GameModel.Direction.UP);
                    moved = gameModel.movePlayer1(-1, 0);
                }
                case S -> {
                    gameModel.setPlayer1Direction(GameModel.Direction.DOWN);
                    moved = gameModel.movePlayer1(1, 0);
                }
                case Q -> {
                    gameModel.setPlayer1Direction(GameModel.Direction.LEFT);
                    moved = gameModel.movePlayer1(0, -1);
                }
                case D -> {
                    gameModel.setPlayer1Direction(GameModel.Direction.RIGHT);
                    moved = gameModel.movePlayer1(0, 1);
                }
                case SPACE -> handlePlaceBomb(1);
                
                // Joueur 2
                case I -> {
                    gameModel.setPlayer2Direction(GameModel.Direction.UP);
                    moved = gameModel.movePlayer2(-1, 0);
                }
                case K -> {
                    gameModel.setPlayer2Direction(GameModel.Direction.DOWN);
                    moved = gameModel.movePlayer2(1, 0);
                }
                case J -> {
                    gameModel.setPlayer2Direction(GameModel.Direction.LEFT);
                    moved = gameModel.movePlayer2(0, -1);
                }
                case L -> {
                    gameModel.setPlayer2Direction(GameModel.Direction.RIGHT);
                    moved = gameModel.movePlayer2(0, 1);
                }
                case ENTER -> handlePlaceBomb(2);
            }
            
            if (moved) updateGridDisplay();
            gameGrid.requestFocus();
            
            // Toujours mettre à jour la vue si une touche directionnelle est appuyée
            if (switch (event.getCode()) {
                case Z, S, Q, D, I, K, J, L -> true;
                default -> false;
            }) updateGridDisplay();
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
        } else if (type == GameModel.CellType.PLAYER1) {
            messageLabel.setText("Joueur 1 ici !");
        } else if (type == GameModel.CellType.PLAYER2) {
            messageLabel.setText("Joueur 2 ici !");
        } else if (type == GameModel.CellType.BONUS_RANGE) {
            messageLabel.setText("Bonus : +1 portée de bombe !");
        } else if (type == GameModel.CellType.MALUS_RANGE) {
            messageLabel.setText("Malus : -1 portée de bombe !");
        }
    }
    
    private void handlePlaceBomb(int player) {
        if (gameController.isPaused()) return;
        boolean canPlace = (player == 1) ? gameModel.canPlaceBomb1() : gameModel.canPlaceBomb2();
        if (!canPlace) {
            messageLabel.setText("Nombre maximum de bombes atteint !");
            return;
        }
        if (gameModel.placeBombForPlayer(player)) {
            updateGridDisplay();
            int row = (player == 1) ? gameModel.getPlayer1Row() : gameModel.getPlayer2Row();
            int col = (player == 1) ? gameModel.getPlayer1Col() : gameModel.getPlayer2Col();
            new Thread(() -> {
                try {
                    Thread.sleep(1500); // 1.5 seconde avant explosion
                } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    explodeBomb(row, col, player);
                });
            }).start();
        }
    }
    
    private void explodeBomb(int row, int col, int player) {
        int range = (player == 1) ? gameModel.getBombRange1() : gameModel.getBombRange2();
        
        boolean player1WasOnBomb = (gameModel.getPlayer1Row() == row && gameModel.getPlayer1Col() == col && gameModel.isBombUnderPlayer1());
        boolean player2WasOnBomb = (gameModel.getPlayer2Row() == row && gameModel.getPlayer2Col() == col && gameModel.isBombUnderPlayer2());
        
        gameModel.setCellType(row, col, GameModel.CellType.EXPLOSION);
        
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
                } else if (t == GameModel.CellType.BOMB1) {
                    explodeBomb(r, c, 1);
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                } else if (t == GameModel.CellType.BOMB2) {
                    explodeBomb(r, c, 2);
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                } else if (t == GameModel.CellType.PLAYER1) {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                    gameModel.killPlayer(1);
                    messageLabel.setText("Joueur 2 a gagné !");
                    pauseButton.setDisable(true);
                    pauseTimer();
                } else if (t == GameModel.CellType.PLAYER2) {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                    gameModel.killPlayer(2);
                    messageLabel.setText("Joueur 1 a gagné !");
                    pauseButton.setDisable(true);
                    pauseTimer();
                } else if (t == GameModel.CellType.BONUS_RANGE || t == GameModel.CellType.MALUS_RANGE) {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                } else {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                }
            }
        }
        
        updateGridDisplay();
        gameModel.bombExploded(player);
        
        // Nettoyage explosion après 400ms
        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                if (gameModel.getCellType(row, col) == GameModel.CellType.EXPLOSION) {
                    if (player1WasOnBomb && gameModel.getPlayer1Row() == row && gameModel.getPlayer1Col() == col) {
                        gameModel.setCellType(row, col, GameModel.CellType.PLAYER1);
                    } else if (player2WasOnBomb && gameModel.getPlayer2Row() == row && gameModel.getPlayer2Col() == col) {
                        gameModel.setCellType(row, col, GameModel.CellType.PLAYER2);
                    } else {
                        gameModel.setCellType(row, col, GameModel.CellType.EMPTY);
                    }
                }
                int[][] dirs2 = { {-1,0}, {1,0}, {0,-1}, {0,1} };
                for (int[] d : dirs2) {
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
    
    private void updateGridDisplay() {
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof StackPane cell) {
                Integer col = GridPane.getColumnIndex(node);
                Integer row = GridPane.getRowIndex(node);
                if (col == null || row == null) continue;
                
                cell.getStyleClass().removeAll(
                        "cell-empty", "cell-wall", "cell-destructible", "cell-player1", "cell-player2", "cell-bomb1", "cell-bomb2",
                        "cell-explosion", "cell-bonus-range", "cell-malus-range"
                );
                cell.getChildren().clear();
                
                boolean hasPlayer1 = (gameModel.getPlayer1Row() == row && gameModel.getPlayer1Col() == col && gameModel.isPlayer1Alive());
                boolean hasPlayer2 = (gameModel.getPlayer2Row() == row && gameModel.getPlayer2Col() == col && gameModel.isPlayer2Alive());
                boolean hasBomb1 = (gameModel.getCellType(row, col) == GameModel.CellType.BOMB1)
                        || (hasPlayer1 && gameModel.isBombUnderPlayer1());
                boolean hasBomb2 = (gameModel.getCellType(row, col) == GameModel.CellType.BOMB2)
                        || (hasPlayer2 && gameModel.isBombUnderPlayer2());
                
                // Affichage superposé
                if (hasPlayer1 && hasBomb1) {
                    cell.getStyleClass().addAll("cell-bomb1", "cell-player1");
                    addPlayerImage(cell, 1);
                } else if (hasPlayer2 && hasBomb2) {
                    cell.getStyleClass().addAll("cell-bomb2", "cell-player2");
                    addPlayerImage(cell, 2);
                } else if (hasPlayer1) {
                    cell.getStyleClass().add("cell-player1");
                    addPlayerImage(cell, 1);
                } else if (hasPlayer2) {
                    cell.getStyleClass().add("cell-player2");
                    addPlayerImage(cell, 2);
                } else {
                    switch (gameModel.getCellType(row, col)) {
                        case WALL -> cell.getStyleClass().add("cell-wall");
                        case DESTRUCTIBLE_WALL -> cell.getStyleClass().add("cell-destructible");
                        case BOMB1 -> cell.getStyleClass().add("cell-bomb1");
                        case BOMB2 -> cell.getStyleClass().add("cell-bomb2");
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
    
    private void addPlayerImage(StackPane cell, int player) {
        try {
            String basePath = (player == 1)
                    ? "/com/example/BomberMan/Personnages/Blanc/"
                    : "/com/example/BomberMan/Personnages/Rouge/";
            
            GameModel.Direction dir = (player == 1)
                    ? gameModel.getPlayer1Direction()
                    : gameModel.getPlayer2Direction();
            
            String imageName;
            switch (dir) {
                case UP -> imageName = "Dos.png";
                case DOWN -> imageName = "Face.png";
                case LEFT -> imageName = "Gauche.png";
                case RIGHT -> imageName = "Droite.png";
                default -> imageName = "Face.png";
            }
            String imagePath = basePath + imageName;
            Image playerImage = new Image(getClass().getResourceAsStream(imagePath));
            ImageView imageView = new ImageView(playerImage);
            imageView.setFitWidth(28);
            imageView.setFitHeight(28);
            cell.getChildren().add(imageView);
        } catch (Exception e) {
            e.printStackTrace(); // Pour voir l'erreur si jamais l'image n'est pas trouvée
        }
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
        stopTimer();
        elapsedSeconds = 0;
    }
    
    @FXML
    private void handleDebugGrid() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < GameModel.getGridHeight(); row++) {
            for (int col = 0; col < GameModel.getGridWidth(); col++) {
                switch (gameModel.getCellType(row, col)) {
                    case WALL -> sb.append("# ");
                    case DESTRUCTIBLE_WALL -> sb.append("D ");
                    case PLAYER1 -> sb.append("1 ");
                    case PLAYER2 -> sb.append("2 ");
                    case BOMB1 -> sb.append("b ");
                    case BOMB2 -> sb.append("x ");
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