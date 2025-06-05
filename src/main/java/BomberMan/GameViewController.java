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
    private Label timerLabel; // Ajouté pour le timer
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

        // Start the game after window is shown (keyboard focus will work)
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
            if (!gameModel.isGameRunning()) return;
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
        int row = gameModel.getPlayerRow();
        int col = gameModel.getPlayerCol();
        if (gameModel.placeBomb(row, col)) {
            updateGridDisplay();
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
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            if (gameModel.isValidPosition(r, c)) {
                GameModel.CellType t = gameModel.getCellType(r, c);
                if (t == GameModel.CellType.DESTRUCTIBLE_WALL) {
                    gameModel.setCellType(r, c, GameModel.CellType.EXPLOSION);
                } else if (t == GameModel.CellType.BOMB) {
                    // Enchaînement des explosions (optionnel)
                    explodeBomb(r, c);
                }
            }
        }
        updateGridDisplay();
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

    private void updateGridDisplay() {
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof StackPane cell) {
                Integer col = GridPane.getColumnIndex(node);
                Integer row = GridPane.getRowIndex(node);
                if (col == null || row == null) continue;
                GameModel.CellType type = gameModel.getCellType(row, col);

                cell.getStyleClass().removeAll("cell-empty", "cell-wall", "cell-destructible", "cell-player", "cell-bomb", "cell-explosion");
                cell.getChildren().clear(); // Important: on enlève les anciennes images

                switch (type) {
                    case WALL -> cell.getStyleClass().add("cell-wall");
                    case DESTRUCTIBLE_WALL -> cell.getStyleClass().add("cell-destructible");
                    case PLAYER -> {
                        cell.getStyleClass().add("cell-player");
                        // Affichage image du personnage
                        try {
                            // Utilise le chemin d'accès du GameModel
                            String imagePath = "/com/example/BomberMan/Personnages/Blanc/Face.png";
                            Image playerImage = new Image(getClass().getResourceAsStream(imagePath));
                            ImageView imageView = new ImageView(playerImage);
                            imageView.setFitWidth(28);
                            imageView.setFitHeight(28);
                            cell.getChildren().add(imageView);
                        } catch (Exception e) {
                            // En cas d'erreur, on ignore l'image (affichage standard)
                        }
                    }
                    case BOMB -> cell.getStyleClass().add("cell-bomb");
                    case EXPLOSION -> cell.getStyleClass().add("cell-explosion");
                    default -> cell.getStyleClass().add("cell-empty");
                }
            }
        }
        scoreLabel.setText(String.valueOf(gameModel.getScore()));
        statusLabel.setText(gameModel.getGameStatus());
    }
    
    // Boutons UI
    
    @FXML
    private void handleStartGame() {
        gameController.startGame();
        updateGridDisplay();
        messageLabel.setText("Partie démarrée !");
        startTimer();
        gameGrid.requestFocus();
        startButton.setDisable(true);
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
        stopTimer();
        gameGrid.requestFocus();
        startButton.setDisable(false); // Réactive le bouton
        pauseButton.setText("Pause");
        pauseButton.setDisable(true); // Désactive le bouton Pause
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
