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

import java.net.URL;

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
    private Timeline aiTimeline;

    private int elapsedSeconds = 0;
    private GameModel gameModel;
    private GameController gameController;
    private boolean vsIA = false;

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
        javafx.application.Platform.runLater(this::handleStartGame);
    }

    public void setVsIA(boolean vsIA) { this.vsIA = vsIA; }
    public void postInit() {
        if (vsIA) setupAITimeline();
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

    private void setupAITimeline() {
        aiTimeline = new Timeline(new KeyFrame(Duration.seconds(0.6), e -> moveAIPlayer()));
        aiTimeline.setCycleCount(Timeline.INDEFINITE);
        aiTimeline.play();
    }

    private void moveAIPlayer() {
        if (!gameModel.isGameRunning() || gameController.isPaused()) return;
        int[][] dirs = { {-1,0}, {1,0}, {0,-1}, {0,1} };
        java.util.List<int[]> dirList = java.util.Arrays.asList(dirs);
        java.util.Collections.shuffle(dirList);
        for (int[] d : dirList) {
            if (gameModel.moveAI(d[0], d[1])) break;
        }
        updateGridDisplay();
    }
    @FXML
    private void handleReturnToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/BomberMan/FXML/MenuView.fxml"));
            Parent menuRoot = loader.load();
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
                cell.setOnMouseClicked(e -> handleCellClick());
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
                case Z -> {
                    moved = gameModel.movePlayer1(-1, 0);
                }
                case S -> {
                    moved = gameModel.movePlayer1(1, 0);
                }
                case Q -> {
                    moved = gameModel.movePlayer1(0, -1);
                }
                case D -> {
                    moved = gameModel.movePlayer1(0, 1);
                }
                case SPACE -> handlePlaceBomb(1);

                case I -> {
                    moved = gameModel.movePlayer2(-1, 0);
                }
                case K -> {
                    moved = gameModel.movePlayer2(1, 0);
                }
                case J -> {
                    moved = gameModel.movePlayer2(0, -1);
                }
                case L -> {
                    moved = gameModel.movePlayer2(0, 1);
                }
                case ENTER -> handlePlaceBomb(2);
            }

            if (moved) updateGridDisplay();
            gameGrid.requestFocus();

            if (switch (event.getCode()) {
                case Z, S, Q, D, I, K, J, L -> true;
                default -> false;
            }) updateGridDisplay();
        });
    }

    private void handleCellClick() {
        gameGrid.requestFocus();
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
                    Thread.sleep(1500);
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

        gameModel.setCellType(row, col, CellType.EXPLOSION);

        int[][] dirs = { {-1,0}, {1,0}, {0,-1}, {0,1} };
        for (int[] d : dirs) {
            for (int dist = 1; dist <= range; dist++) {
                int r = row + d[0] * dist, c = col + d[1] * dist;
                if (!gameModel.isValidPosition(r, c)) break;
                CellType t = gameModel.getCellType(r, c);
                if (t == CellType.WALL) {
                    break;
                } else if (t == CellType.DESTRUCTIBLE_WALL) {
                    gameModel.setCellType(r, c, CellType.EXPLOSION);
                    break;
                } else if (t == CellType.BOMB1) {
                    explodeBomb(r, c, 1);
                    gameModel.setCellType(r, c, CellType.EXPLOSION);
                } else if (t == CellType.BOMB2) {
                    explodeBomb(r, c, 2);
                    gameModel.setCellType(r, c, CellType.EXPLOSION);
                } else if (t == CellType.PLAYER1) {
                    gameModel.setCellType(r, c, CellType.EXPLOSION);
                    gameModel.killPlayer(1);
                    messageLabel.setText("Joueur 2 a gagné !");
                    pauseButton.setDisable(true);
                    pauseTimer();
                } else if (t == CellType.PLAYER2) {
                    gameModel.setCellType(r, c, CellType.EXPLOSION);
                    gameModel.killPlayer(2);
                    messageLabel.setText("Joueur 1 a gagné !");
                    pauseButton.setDisable(true);
                    pauseTimer();
                } else if (t == CellType.BONUS_RANGE || t == CellType.MALUS_RANGE) {
                    gameModel.setCellType(r, c, CellType.EXPLOSION);
                } else {
                    gameModel.setCellType(r, c, CellType.EXPLOSION);
                }
            }
        }

        updateGridDisplay();
        gameModel.bombExploded(player);

        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                if (gameModel.getCellType(row, col) == CellType.EXPLOSION) {
                    if (player1WasOnBomb && gameModel.getPlayer1Row() == row && gameModel.getPlayer1Col() == col) {
                        gameModel.setCellType(row, col, CellType.PLAYER1);
                    } else if (player2WasOnBomb && gameModel.getPlayer2Row() == row && gameModel.getPlayer2Col() == col) {
                        gameModel.setCellType(row, col, CellType.PLAYER2);
                    } else {
                        gameModel.setCellType(row, col, CellType.EMPTY);
                    }
                }
                int[][] dirs2 = { {-1,0}, {1,0}, {0,-1}, {0,1} };
                for (int[] d : dirs2) {
                    for (int dist = 1; dist <= range; dist++) {
                        int r = row + d[0] * dist, c = col + d[1] * dist;
                        if (gameModel.isValidPosition(r, c)) {
                            if (gameModel.getCellType(r, c) == CellType.EXPLOSION) {
                                gameModel.setCellType(r, c, CellType.EMPTY);
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
        if (aiTimeline != null) aiTimeline.stop();
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
                boolean hasBomb1 = (gameModel.getCellType(row, col) == CellType.BOMB1)
                        || (hasPlayer1 && gameModel.isBombUnderPlayer1());
                boolean hasBomb2 = (gameModel.getCellType(row, col) == CellType.BOMB2)
                        || (hasPlayer2 && gameModel.isBombUnderPlayer2());

                // Affichage superposé
                if (hasPlayer1 && hasBomb1) {
                    cell.getStyleClass().addAll("cell-bomb1", "cell-player1");
                    addPlayerImage(cell, "/com/example/BomberMan/Personnages/Blanc/Face.png");
                } else if (hasPlayer2 && hasBomb2) {
                    cell.getStyleClass().addAll("cell-bomb2", "cell-player2");
                    addPlayerImage(cell, "/com/example/BomberMan/Personnages/Blanc/Face.png");
                } else if (hasPlayer1) {
                    cell.getStyleClass().add("cell-player1");
                    addPlayerImage(cell, "/com/example/BomberMan/Personnages/Blanc/Face.png");
                } else if (hasPlayer2) {
                    cell.getStyleClass().add("cell-player2");
                    addPlayerImage(cell, "/com/example/BomberMan/Personnages/Blanc/Face.png");
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

    private void addPlayerImage(StackPane cell, String imgPath) {
        URL imgUrl = getClass().getResource(imgPath);
        if (imgUrl == null) {
            System.out.println("Ressource image non trouvée : " + imgPath);
            return;
        }
        ImageView imgView = new ImageView(new Image(imgUrl.toExternalForm()));
        imgView.setFitWidth(32);
        imgView.setFitHeight(32);
        cell.getChildren().add(imgView);
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
            if (aiTimeline != null) aiTimeline.pause();
            pauseButton.setText("Reprendre");
        } else {
            messageLabel.setText("Jeu relancé !");
            resumeTimer();
            if (aiTimeline != null) aiTimeline.play();
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
        if (aiTimeline != null) {
            aiTimeline.stop();
            aiTimeline = null;
        }
        if (vsIA) setupAITimeline();
    }
}
