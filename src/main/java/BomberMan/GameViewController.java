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
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

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
    private void pauseTimer() { if (timer != null) timer.pause(); }
    private void resumeTimer() { if (timer != null) timer.play(); }
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
            if (event.getCode() == KeyCode.Z) {
                gameModel.setPlayerDirection(GameModel.Direction.UP);
                moved = gameModel.movePlayer(-1, 0);
            }
            if (event.getCode() == KeyCode.S) {
                gameModel.setPlayerDirection(GameModel.Direction.DOWN);
                moved = gameModel.movePlayer(1, 0);
            }
            if (event.getCode() == KeyCode.Q) {
                gameModel.setPlayerDirection(GameModel.Direction.LEFT);
                moved = gameModel.movePlayer(0, -1);
            }
            if (event.getCode() == KeyCode.D) {
                gameModel.setPlayerDirection(GameModel.Direction.RIGHT);
                moved = gameModel.movePlayer(0, 1);
            }
            if (event.getCode() == KeyCode.SPACE) handlePlaceBomb();
            if (event.getCode() == KeyCode.Z || event.getCode() == KeyCode.S ||
                    event.getCode() == KeyCode.Q || event.getCode() == KeyCode.D) {
                updateGridDisplay();
            }
        });
    }

    private void handleCellClick(int row, int col) {
        GameModel.CellType type = gameModel.getCellType(row, col);
        switch (type) {
            case DESTRUCTIBLE_WALL -> {
                boolean destroyed = gameController.destroyWall(row, col);
                if (destroyed) {
                    messageLabel.setText("Mur destructible détruit !");
                    updateGridDisplay();
                }
            }
            case EMPTY -> messageLabel.setText("Case vide");
            case WALL -> messageLabel.setText("Mur indestructible");
            case PLAYER -> messageLabel.setText("Le joueur est ici !");
            case BONUS_RANGE -> messageLabel.setText("Bonus : +1 portée de bombe !");
            case MALUS_RANGE -> messageLabel.setText("Malus : -1 portée de bombe !");
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
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> explodeBomb(row, col));
            }).start();
        }
    }

    private void explodeBomb(int row, int col) {
        int range = gameModel.getBombRange();
        boolean playerWasOnBomb = (gameModel.getPlayerRow() == row && gameModel.getPlayerCol() == col && gameModel.isBombUnderPlayer());
        gameModel.setCellType(row, col, GameModel.CellType.EXPLOSION);

        int[][] dirs = { {-1,0}, {1,0}, {0,-1}, {0,1} };
        for (int[] d : dirs) {
            for (int dist = 1; dist <= range; dist++) {
                int r = row + d[0] * dist, c = col + d[1] * dist;
                if (!gameModel.isValidPosition(r, c)) break;
                GameModel.CellType t = gameModel.getCellType(r, c);
                if (t == GameModel.CellType.WALL) break;
                else if (t == GameModel.CellType.DESTRUCTIBLE_WALL) {
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

        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                if (gameModel.getCellType(row, col) == GameModel.CellType.EXPLOSION) {
                    if (playerWasOnBomb && gameModel.getPlayerRow() == row && gameModel.getPlayerCol() == col) {
                        gameModel.setCellType(row, col, GameModel.CellType.PLAYER);
                    } else {
                        gameModel.setCellType(row, col, GameModel.CellType.EMPTY);
                    }
                }
                int[][] dirs2 = { {-1,0}, {1,0}, {0,-1}, {0,1} };
                for (int[] d : dirs2) {
                    for (int dist = 1; dist <= range; dist++) {
                        int r = row + d[0] * dist, c = col + d[1] * dist;
                        if (gameModel.isValidPosition(r, c) &&
                                gameModel.getCellType(r, c) == GameModel.CellType.EXPLOSION) {
                            gameModel.setCellType(r, c, GameModel.CellType.EMPTY);
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
        pauseTimer();
        pauseButton.setDisable(true);
        updateGridDisplay();
        if (aiTimeline != null) aiTimeline.stop();
    }

    private void updateGridDisplay() {
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof StackPane cell) {
                Integer col = GridPane.getColumnIndex(node);
                Integer row = GridPane.getRowIndex(node);
                if (col == null || row == null) continue;

                cell.getStyleClass().removeAll("cell-empty", "cell-wall", "cell-destructible", "cell-player", "cell-bomb", "cell-explosion", "cell-ai", "cell-bonus-range", "cell-malus-range");
                cell.getChildren().clear();

                boolean hasPlayer = (gameModel.getPlayerRow() == row && gameModel.getPlayerCol() == col);
                boolean hasBomb = (gameModel.getCellType(row, col) == GameModel.CellType.BOMB);
                boolean hasAI = (gameModel.getAIRow() == row && gameModel.getAICol() == col);

                if (hasPlayer && hasBomb) {
                    cell.getStyleClass().addAll("cell-bomb", "cell-player");
                    try {
                        String imagePath = getPlayerImagePath(gameModel.getPlayerDirection());
                        Image playerImage = new Image(getClass().getResourceAsStream(imagePath));
                        ImageView imageView = new ImageView(playerImage);
                        imageView.setFitWidth(28);
                        imageView.setFitHeight(28);
                        cell.getChildren().add(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (hasPlayer) {
                    cell.getStyleClass().add("cell-player");
                    try {
                        String imagePath = getPlayerImagePath(gameModel.getPlayerDirection());
                        Image playerImage = new Image(getClass().getResourceAsStream(imagePath));
                        ImageView imageView = new ImageView(playerImage);
                        imageView.setFitWidth(28);
                        imageView.setFitHeight(28);
                        cell.getChildren().add(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (hasAI) {
                    cell.getStyleClass().add("cell-ai");
                    try {
                        String imagePath = "/com/example/BomberMan/Personnages/Blanc/IA.png";
                        Image aiImage = new Image(getClass().getResourceAsStream(imagePath));
                        ImageView imageView = new ImageView(aiImage);
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

    private String getPlayerImagePath(GameModel.Direction direction) {
        return switch (direction) {
            case UP -> "/com/example/BomberMan/Personnages/Blanc/Dos.png";
            case DOWN -> "/com/example/BomberMan/Personnages/Blanc/Face.png";
            case LEFT -> "/com/example/BomberMan/Personnages/Blanc/Gauche.png";
            case RIGHT -> "/com/example/BomberMan/Personnages/Blanc/Droite.png";
        };
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
        gameModel.setPlayerDirection(GameModel.Direction.DOWN);
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
    @FXML
    private void handleReturnToMenu() {
        try {
            // Charge le menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/BomberMan/FXML/MenuView.fxml"));
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