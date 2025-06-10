package BomberMan.controller;

import BomberMan.application.BomberManApp;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import BomberMan.Direction;

public class GameController {
    @FXML
    private GridPane gridPane;
    @FXML
    private HBox topBar;
    @FXML
    private Label timerLabel;
    @FXML
    private Label scoreP1Label;
    @FXML
    private Label scoreP2Label;
    @FXML
    private Button backMenuButton;
    @FXML
    private ImageView p1Icon;
    @FXML
    private ImageView p2Icon;
    @FXML
    private HBox bottomBar;
    @FXML
    private Button btnPerso;

    private final int rows = 13, cols = 15;
    private final int cellSize = 40;

    private String[][] map = new String[rows][cols];
    private Bomb[][] bombs = new Bomb[rows][cols];
    private boolean[][] isExplosion = new boolean[rows][cols];

    // Personnalisation couleurs des joueurs
    private final String[] COLORS = {"Blanc", "Rouge", "Bleu", "Noir"};
    private final String[] COLOR_KEYS = {"Blanc", "Rouge", "Bleu", "Noir"};
    private int indexJ1 = 0;
    private int indexJ2 = 1;

    // Personnalisation thème de la map (ajout Backrooms)
    private final String[] THEMES = {"Base", "Jungle", "Désert", "Backrooms"};
    private final String[] THEME_FOLDERS = {"asset_base", "asset_jungle", "asset_desert", "backrooms_asset"};
    private int themeIndex = 0; // 0: base, 1: jungle, 2: desert, 3: backrooms

    private Image pelouseImg, wallImg, destructibleImg, bombImg, explosionImg;
    private ImageView[][] cellBackgrounds = new ImageView[rows][cols];
    private ImageView[][] cellExplosions  = new ImageView[rows][cols];
    private ImageView[][] cellBombs       = new ImageView[rows][cols];
    private ImageView[][] cellPlayer1     = new ImageView[rows][cols];
    private ImageView[][] cellPlayer2     = new ImageView[rows][cols];

    private int p1Row = 1, p1Col = 1;
    private Direction p1Dir = Direction.DOWN;
    private int p1BombCount = 0;
    private int p1ExplosionRadius = 1;
    private int scoreP1 = 0;
    private boolean p1Alive = true;

    private int p2Row = rows - 2, p2Col = cols - 2;
    private Direction p2Dir = Direction.DOWN;
    private int p2BombCount = 0;
    private int p2ExplosionRadius = 1;
    private int scoreP2 = 0;
    private boolean p2Alive = true;

    private Timeline timerTimeline;
    private int elapsedSeconds = 0;

    private boolean gameEnded = false;

    private Scene gameScene; // pour revenir à la scène de jeu d'origine
    @FXML
    private Button btnRestartMusic;
    @FXML
    private ImageView restartIcon;
    @FXML
    private Button btnPauseMusic;
    @FXML
    private Button btnNextMusic;
    @FXML
    private ImageView pauseIcon;
    @FXML
    private ImageView nextIcon;
    
    private List<String> musicFiles = Arrays.asList(
            "/Musique/background1.mp3",
            "/Musique/background2.mp3",
            "/Musique/background3.mp3",
            "/Musique/background4.mp3",
            "/Musique/background5.mp3",
            "/Musique/background6.mp3",
            "/Musique/background7.mp3",
            "/Musique/background8.mp3",
            "/Musique/background9.mp3",
            "/Musique/background10.mp3",
            "/Musique/background11.mp3",
            "/Musique/background12.mp3",
            "/Musique/background13.mp3",
            "/Musique/background14.mp3"
            // Ajoute les chemins de tes musiques ici
    );
    private int currentMusicIndex = 0;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPaused = false;
    
    private static class Bomb {
        int row, col;
        int owner;
        int radius;
        public Bomb(int row, int col, int owner, int radius) {
            this.row = row; this.col = col; this.owner = owner; this.radius = radius;
        }
    }

    @FXML
    public void initialize() {
        loadThemeAssets();

        if (p1Icon != null) p1Icon.setImage(getPlayerImage(COLOR_KEYS[indexJ1], Direction.DOWN));
        if (p2Icon != null) p2Icon.setImage(getPlayerImage(COLOR_KEYS[indexJ2], Direction.DOWN));

        gridPane.getChildren().clear();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(cellSize, cellSize);

                ImageView bg    = new ImageView(); bg.setFitWidth(cellSize); bg.setFitHeight(cellSize);
                cellBackgrounds[r][c] = bg;

                ImageView expl  = new ImageView(); expl.setFitWidth(cellSize); expl.setFitHeight(cellSize);
                expl.setVisible(false); cellExplosions[r][c] = expl;

                ImageView bomb  = new ImageView(); bomb.setFitWidth(cellSize); bomb.setFitHeight(cellSize);
                bomb.setVisible(false); cellBombs[r][c] = bomb;

                ImageView p1    = new ImageView(); p1.setFitWidth(cellSize); p1.setFitHeight(cellSize);
                p1.setVisible(false); cellPlayer1[r][c] = p1;

                ImageView p2    = new ImageView(); p2.setFitWidth(cellSize); p2.setFitHeight(cellSize);
                p2.setVisible(false); cellPlayer2[r][c] = p2;

                cell.getChildren().addAll(bg, expl, bomb, p1, p2);
                gridPane.add(cell, c, r);
            }
        }
        generateRandomMap();
        updateBombs();
        updateExplosions();
        updatePlayersDisplay();

        if (timerLabel != null) timerLabel.setText("00:00");
        if (scoreP1Label != null) scoreP1Label.setText("0");
        if (scoreP2Label != null) scoreP2Label.setText("0");

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);

        gridPane.setFocusTraversable(true);
        gridPane.requestFocus();
        gridPane.setOnKeyPressed(this::handleKeyPressed);
        gridPane.setOnMouseClicked(e -> gridPane.requestFocus());

        if (backMenuButton != null)
            backMenuButton.setOnAction(e -> onBackMenu());

        if (gridPane != null && gridPane.getScene() != null) {
            gameScene = gridPane.getScene();
        }

        if (btnPerso != null) {
            btnPerso.setOnAction(e -> openPersonnalisationPage());
        }
        if (pauseIcon != null) {
            pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
        }
        if (nextIcon != null) {
            nextIcon.setImage(new Image(getClass().getResource("/images/pass.png").toExternalForm()));
        }
        if (btnNextMusic != null) {
            btnNextMusic.setOnAction(e -> playNextMusic());
        }
        if (btnPauseMusic != null) {
            btnPauseMusic.setOnAction(e -> {
                if (mediaPlayer == null) return;
                if (isMusicPaused) {
                    mediaPlayer.play();
                    if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
                    isMusicPaused = false;
                } else {
                    mediaPlayer.pause();
                    if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/play.png").toExternalForm()));
                    isMusicPaused = true;
                }
            });
        }
        if (restartIcon != null) {
            restartIcon.setImage(new Image(getClass().getResource("/images/revenir.png").toExternalForm()));
        }
        if (btnRestartMusic != null) {
            btnRestartMusic.setOnAction(e -> {
                if (mediaPlayer != null) {
                    mediaPlayer.seek(javafx.util.Duration.ZERO);
                    if (isMusicPaused) {
                        mediaPlayer.play();
                        isMusicPaused = false;
                        pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
                    }
                }
            });
        }
        playMusic(currentMusicIndex);
    }

    private void loadThemeAssets() {
        String folder = THEME_FOLDERS[themeIndex];
        pelouseImg      = loadAsset(folder, "pelouse.png");
        wallImg         = loadAsset(folder, "wall.png");
        destructibleImg = loadAsset(folder, "destructible.png");
        bombImg         = loadAsset(folder, "bomb.png");
        explosionImg    = loadAsset(folder, "explosion.png");
    }

    private Image loadAsset(String folder, String file) {
        String path = "/images/" + folder + "/" + file;
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Asset manquant: " + path + ", fallback sur asset_base");
            url = getClass().getResource("/images/asset_base/" + file);
        }
        if (url == null) {
            System.err.println("Image absente aussi dans asset_base: " + file);
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9W5x3QAAAABJRU5ErkJggg==");
        }
        return new Image(url.toString());
    }
    
    private void playMusic(int index) {
        try {
            if (mediaPlayer != null) mediaPlayer.stop();
            String musicFile = musicFiles.get(index);
            java.net.URL url = getClass().getResource(musicFile);
            if (url == null) {
                System.err.println("Musique introuvable : " + musicFile);
                return;
            }
            Media media = new Media(url.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(this::playNextMusic);
            mediaPlayer.play();
            isMusicPaused = false; // ← On remet l'état à "en lecture"
            if (pauseIcon != null)
                pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la musique : " + e.getMessage());
        }
    }
    
    private void playNextMusic() {
        currentMusicIndex = (currentMusicIndex + 1) % musicFiles.size();
        playMusic(currentMusicIndex);
    }
    public void initGame(boolean is1v1) {
        scoreP1 = 0;
        scoreP2 = 0;
        gameEnded = false;
        p1Row = 1; p1Col = 1; p1Dir = Direction.DOWN; p1BombCount = 0; p1ExplosionRadius = 1; p1Alive = true;
        p2Row = rows - 2; p2Col = cols - 2; p2Dir = Direction.DOWN; p2BombCount = 0; p2ExplosionRadius = 1; p2Alive = true;

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                bombs[r][c] = null;
                isExplosion[r][c] = false;
            }
        generateRandomMap();
        updateBombs();
        updateExplosions();
        updatePlayersDisplay();

        elapsedSeconds = 0;
        if (timerLabel != null) timerLabel.setText("00:00");
        if (scoreP1Label != null) scoreP1Label.setText(String.valueOf(scoreP1));
        if (scoreP2Label != null) scoreP2Label.setText(String.valueOf(scoreP2));

        timerTimeline.stop();
        timerTimeline.playFromStart();

        gridPane.requestFocus();
    }

    private void generateRandomMap() {
        Random rand = new Random();
        int[][] joueurs = {{1, 1}, {rows - 2, cols - 2}};
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1 || (r % 2 == 0 && c % 2 == 0)) {
                    map[r][c] = "wall";
                } else {
                    boolean nearPlayer = false;
                    for (int[] pos : joueurs) {
                        if (Math.abs(r - pos[0]) <= 1 && Math.abs(c - pos[1]) <= 1) {
                            nearPlayer = true; break;
                        }
                    }
                    if (nearPlayer) map[r][c] = "pelouse";
                    else map[r][c] = (rand.nextDouble() < 0.45) ? "destructible" : "pelouse";
                }
            }
        }
        drawBoard();
    }

    private void drawBoard() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                Image img;
                switch (map[r][c]) {
                    case "wall":         img = wallImg; break;
                    case "destructible": img = destructibleImg; break;
                    default:             img = pelouseImg;
                }
                cellBackgrounds[r][c].setImage(img);
            }
    }

    private void updateBombs() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                boolean has = bombs[r][c] != null;
                cellBombs[r][c].setVisible(has);
                if (has) cellBombs[r][c].setImage(bombImg);
            }
    }

    private void updateExplosions() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                cellExplosions[r][c].setVisible(isExplosion[r][c]);
                if (isExplosion[r][c]) cellExplosions[r][c].setImage(explosionImg);
            }
    }

    private void updatePlayersDisplay() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                cellPlayer1[r][c].setVisible(false);
                cellPlayer2[r][c].setVisible(false);
            }
        if (p1Alive && !map[p1Row][p1Col].equals("wall") && !map[p1Row][p1Col].equals("destructible")) {
            cellPlayer1[p1Row][p1Col].setImage(getPlayerImage(COLOR_KEYS[indexJ1], p1Dir));
            cellPlayer1[p1Row][p1Col].setVisible(true);
        }
        if (p2Alive && !map[p2Row][p2Col].equals("wall") && !map[p2Row][p2Col].equals("destructible")) {
            cellPlayer2[p2Row][p2Col].setImage(getPlayerImage(COLOR_KEYS[indexJ2], p2Dir));
            cellPlayer2[p2Row][p2Col].setVisible(true);
        }
    }

    private Image getPlayerImage(String colorKey, Direction dir) {
        String suffix;
        switch (dir) {
            case UP:    suffix = "Dos"; break;
            case LEFT: suffix = "Gauche"; break;
            case RIGHT: suffix = "Droite"; break;
            default:     suffix = "Face"; break;
        }
        // NE PAS METTRE /images/, les personnages sont dans ressources/personnages/...
        String path = "/Personnages/" + colorKey + "/" + suffix + ".png";
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Image non trouvée : " + path);
            url = getClass().getResource("/Personnages/Blanc/face.png");
        }
        if (url == null) {
            System.err.println("Fallback impossible : image joueur manquante.");
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9W5x3QAAAABJRU5ErkJggg==");
        }
        return new Image(url.toString());
    }

    private void handleKeyPressed(KeyEvent event) {
        if (gameEnded) return;
        if (!p1Alive && !p2Alive) return;

        KeyCode code = event.getCode();

        int newRow1 = p1Row, newCol1 = p1Col;
        Direction newDir1 = p1Dir;
        boolean moved1 = false, bomb1 = false;
        switch (code) {
            case Z: newRow1--; newDir1 = Direction.UP; moved1 = true; break;
            case S: newRow1++; newDir1 = Direction.DOWN; moved1 = true; break;
            case Q: newCol1--; newDir1 = Direction.LEFT; moved1 = true; break;
            case D: newCol1++; newDir1 = Direction.RIGHT; moved1 = true; break;
            case E: bomb1 = true; break;
        }
        if (moved1 && p1Alive) {
            if (isWalkable(newRow1, newCol1, 1)) {
                p1Row = newRow1;
                p1Col = newCol1;
            }
            p1Dir = newDir1;
            updatePlayersDisplay();
            return;
        }
        if (bomb1 && p1Alive) {
            placeBomb(p1Row, p1Col, 1, p1ExplosionRadius);
            return;
        }

        int newRow2 = p2Row, newCol2 = p2Col;
        Direction newDir2 = p2Dir;
        boolean moved2 = false, bomb2 = false;
        switch (code) {
            case I: newRow2--; newDir2 = Direction.UP; moved2 = true; break;
            case K: newRow2++; newDir2 = Direction.DOWN; moved2 = true; break;
            case J: newCol2--; newDir2 = Direction.LEFT; moved2 = true; break;
            case L: newCol2++; newDir2 = Direction.RIGHT; moved2 = true; break;
            case U: bomb2 = true; break;
        }
        if (moved2 && p2Alive) {
            if (isWalkable(newRow2, newCol2, 2)) {
                p2Row = newRow2;
                p2Col = newCol2;
            }
            p2Dir = newDir2;
            updatePlayersDisplay();
            return;
        }
        if (bomb2 && p2Alive) {
            placeBomb(p2Row, p2Col, 2, p2ExplosionRadius);
        }
    }

    private boolean isWalkable(int row, int col, int playerNum) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        if (!map[row][col].equals("pelouse")) return false;
        if (bombs[row][col] != null) return false;
        if (playerNum == 1 && row == p2Row && col == p2Col) return false;
        if (playerNum == 2 && row == p1Row && col == p1Col) return false;
        return true;
    }

    private void placeBomb(int row, int col, int owner, int radius) {
        if (!map[row][col].equals("pelouse") || bombs[row][col] != null) return;
        if (owner == 1 && p1BombCount >= 2) return;
        if (owner == 2 && p2BombCount >= 2) return;
        bombs[row][col] = new Bomb(row, col, owner, radius);
        if (owner == 1) p1BombCount++;
        if (owner == 2) p2BombCount++;
        updateBombs();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> explodeBomb(row, col)));
        timeline.setCycleCount(1);
        timeline.play();
    }

    private void explodeBomb(int row, int col) {
        Bomb bomb = bombs[row][col];
        if (bomb == null) return;
        bombs[row][col] = null;
        if (bomb.owner == 1 && p1BombCount > 0) p1BombCount--;
        if (bomb.owner == 2 && p2BombCount > 0) p2BombCount--;

        List<int[]> explosionCells = new ArrayList<>();
        explosionCells.add(new int[]{row, col});
        for (int[] dir : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            for (int r=1; r<=bomb.radius; r++) {
                int nr = row + dir[0]*r, nc = col + dir[1]*r;
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) break;
                if (map[nr][nc].equals("wall")) break;
                explosionCells.add(new int[]{nr, nc});
                if (map[nr][nc].equals("destructible")) break;
            }
        }
        for (int[] cell : explosionCells) isExplosion[cell[0]][cell[1]] = true;
        updateBombs();
        updateExplosions();
        updatePlayersDisplay();

        AtomicBoolean p1Killed = new AtomicBoolean(false);
        AtomicBoolean p2Killed = new AtomicBoolean(false);

        for (int[] cell : explosionCells) {
            int r = cell[0], c = cell[1];
            if (p1Alive && r == p1Row && c == p1Col) p1Killed.set(true);
            if (p2Alive && r == p2Row && c == p2Col) p2Killed.set(true);
            if (map[r][c].equals("destructible")) {
                map[r][c] = "pelouse";
            }
            if (bombs[r][c] != null) explodeBomb(r, c);
        }
        drawBoard();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            for (int[] cell : explosionCells) isExplosion[cell[0]][cell[1]] = false;
            updateExplosions();
            updatePlayersDisplay();
            if (!gameEnded && (p1Killed.get() || p2Killed.get())) {
                if (p1Killed.get()) addScore(2, 1);
                if (p2Killed.get()) addScore(1, 1);

                if (scoreP1 >= 3) {
                    javafx.application.Platform.runLater(() -> showWinner(1));
                } else if (scoreP2 >= 3) {
                    javafx.application.Platform.runLater(() -> showWinner(2));
                } else {
                    restartRound();
                }
            }
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    private void restartRound() {
        p1Row = 1; p1Col = 1; p1Dir = Direction.DOWN; p1BombCount = 0; p1ExplosionRadius = 1; p1Alive = true;
        p2Row = rows - 2; p2Col = cols - 2; p2Dir = Direction.DOWN; p2BombCount = 0; p2ExplosionRadius = 1; p2Alive = true;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                bombs[r][c] = null;
                isExplosion[r][c] = false;
            }
        generateRandomMap();
        updateBombs();
        updateExplosions();
        updatePlayersDisplay();
        gridPane.requestFocus();
    }

    private void showWinner(int player) {
        gameEnded = true;
        if (timerTimeline != null) timerTimeline.stop();

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Victoire !");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);

        String msg = (player == 1) ? "Le joueur 1 a gagné la partie !" : "Le joueur 2 a gagné la partie !";
        Label label = new Label(msg);
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ImageView winnerImg;
        if (player == 1) {
            winnerImg = new ImageView(getPlayerImage(COLOR_KEYS[indexJ1], Direction.DOWN));
        } else {
            winnerImg = new ImageView(getPlayerImage(COLOR_KEYS[indexJ2], Direction.DOWN));
        }
        winnerImg.setFitHeight(80); winnerImg.setFitWidth(80);

        Button backBtn = new Button("Retour au menu");
        backBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #b00; -fx-text-fill: white;");
        backBtn.setOnAction(e -> {
            dialog.close();
            BomberManApp.showMenu();
        });

        vbox.getChildren().addAll(label, winnerImg, backBtn);
        Scene scene = new Scene(vbox, 350, 250);
        dialog.setScene(scene);
        dialog.show();
    }

    private void openPersonnalisationPage() {
        if (gridPane == null || gridPane.getScene() == null) return;
        Stage stage = (Stage) gridPane.getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();

        VBox root = new VBox(32);
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #222;");

        Label titre = new Label("Personnalisation");
        titre.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        root.getChildren().add(titre);

        final int[] tempIndexJ1 = {indexJ1};
        final int[] tempIndexJ2 = {indexJ2};
        final int[] tempThemeIndex = {themeIndex};

        HBox ligneJ1 = new HBox(16);
        ligneJ1.setAlignment(Pos.CENTER);
        Label joueur1Label = new Label("Joueur 1");
        joueur1Label.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");
        Button leftJ1 = new Button("<");
        leftJ1.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");
        Button rightJ1 = new Button(">");
        rightJ1.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");
        Label couleurJ1Label = new Label(COLORS[tempIndexJ1[0]]);
        couleurJ1Label.setStyle("-fx-font-size: 22px; -fx-text-fill: #fff; -fx-font-weight: bold;");
        ImageView imageJ1 = new ImageView(getPlayerImage(COLOR_KEYS[tempIndexJ1[0]], Direction.DOWN));
        imageJ1.setFitWidth(52); imageJ1.setFitHeight(52); imageJ1.setPreserveRatio(true);
        leftJ1.setOnAction(e -> {
            tempIndexJ1[0] = (tempIndexJ1[0] - 1 + COLORS.length) % COLORS.length;
            couleurJ1Label.setText(COLORS[tempIndexJ1[0]]);
            imageJ1.setImage(getPlayerImage(COLOR_KEYS[tempIndexJ1[0]], Direction.DOWN));
        });
        rightJ1.setOnAction(e -> {
            tempIndexJ1[0] = (tempIndexJ1[0] + 1) % COLORS.length;
            couleurJ1Label.setText(COLORS[tempIndexJ1[0]]);
            imageJ1.setImage(getPlayerImage(COLOR_KEYS[tempIndexJ1[0]], Direction.DOWN));
        });
        ligneJ1.getChildren().addAll(joueur1Label, leftJ1, couleurJ1Label, rightJ1, imageJ1);

        HBox ligneJ2 = new HBox(16);
        ligneJ2.setAlignment(Pos.CENTER);
        Label joueur2Label = new Label("Joueur 2");
        joueur2Label.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");
        Button leftJ2 = new Button("<");
        leftJ2.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");
        Button rightJ2 = new Button(">");
        rightJ2.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");
        Label couleurJ2Label = new Label(COLORS[tempIndexJ2[0]]);
        couleurJ2Label.setStyle("-fx-font-size: 22px; -fx-text-fill: #fff; -fx-font-weight: bold;");
        ImageView imageJ2 = new ImageView(getPlayerImage(COLOR_KEYS[tempIndexJ2[0]], Direction.DOWN));
        imageJ2.setFitWidth(52); imageJ2.setFitHeight(52); imageJ2.setPreserveRatio(true);
        leftJ2.setOnAction(e -> {
            tempIndexJ2[0] = (tempIndexJ2[0] - 1 + COLORS.length) % COLORS.length;
            couleurJ2Label.setText(COLORS[tempIndexJ2[0]]);
            imageJ2.setImage(getPlayerImage(COLOR_KEYS[tempIndexJ2[0]], Direction.DOWN));
        });
        rightJ2.setOnAction(e -> {
            tempIndexJ2[0] = (tempIndexJ2[0] + 1) % COLORS.length;
            couleurJ2Label.setText(COLORS[tempIndexJ2[0]]);
            imageJ2.setImage(getPlayerImage(COLOR_KEYS[tempIndexJ2[0]], Direction.DOWN));
        });
        ligneJ2.getChildren().addAll(joueur2Label, leftJ2, couleurJ2Label, rightJ2, imageJ2);

        HBox ligneTheme = new HBox(16);
        ligneTheme.setAlignment(Pos.CENTER);
        Label mapLabel = new Label("Map :");
        mapLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");
        Button leftTheme = new Button("<");
        leftTheme.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");
        Button rightTheme = new Button(">");
        rightTheme.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");
        Label themeNameLabel = new Label(THEMES[tempThemeIndex[0]]);
        themeNameLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #fff; -fx-font-weight: bold;");
        ImageView themeImg = new ImageView(getThemePreviewImage(THEME_FOLDERS[tempThemeIndex[0]]));
        themeImg.setFitWidth(90); themeImg.setFitHeight(52); themeImg.setPreserveRatio(true);

        leftTheme.setOnAction(e -> {
            tempThemeIndex[0] = (tempThemeIndex[0] - 1 + THEMES.length) % THEMES.length;
            themeNameLabel.setText(THEMES[tempThemeIndex[0]]);
            themeImg.setImage(getThemePreviewImage(THEME_FOLDERS[tempThemeIndex[0]]));
        });
        rightTheme.setOnAction(e -> {
            tempThemeIndex[0] = (tempThemeIndex[0] + 1) % THEMES.length;
            themeNameLabel.setText(THEMES[tempThemeIndex[0]]);
            themeImg.setImage(getThemePreviewImage(THEME_FOLDERS[tempThemeIndex[0]]));
        });

        ligneTheme.getChildren().addAll(mapLabel, leftTheme, themeNameLabel, rightTheme, themeImg);

        Button applyBtn = new Button("Appliquer");
        applyBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #4090c0; -fx-text-fill: white; -fx-padding: 12 32 12 32; -fx-background-radius: 8px;");
        applyBtn.setOnAction(e -> {
            indexJ1 = tempIndexJ1[0];
            indexJ2 = tempIndexJ2[0];
            themeIndex = tempThemeIndex[0];
            loadThemeAssets();
            drawBoard();
            updatePlayersDisplay();
            if (p1Icon != null) p1Icon.setImage(getPlayerImage(COLOR_KEYS[indexJ1], Direction.DOWN));
            if (p2Icon != null) p2Icon.setImage(getPlayerImage(COLOR_KEYS[indexJ2], Direction.DOWN));
            if (gameScene != null) {
                stage.setScene(gameScene);
            }
        });

        root.getChildren().addAll(ligneJ1, ligneJ2, ligneTheme, applyBtn);

        Scene persoScene = new Scene(root, width, height);
        stage.setScene(persoScene);
    }

    private Image getThemePreviewImage(String themeFolder) {
        String path = "/images/" + themeFolder + "/map.png";
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Image de thème non trouvée : " + path);
            url = getClass().getResource("/images/asset_base/map.png");
        }
        if (url == null) {
            System.err.println("Image absente aussi dans asset_base: map.png");
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9W5x3QAAAABJRU5ErkJggg==");
        }
        return new Image(url.toString());
    }

    private void updateTimer() {
        elapsedSeconds++;
        int min = elapsedSeconds / 60;
        int sec = elapsedSeconds % 60;
        if (timerLabel != null)
            timerLabel.setText(String.format("%02d:%02d", min, sec));
    }

    private void addScore(int player, int points) {
        if (player == 1) {
            scoreP1 += points;
            if (scoreP1Label != null) scoreP1Label.setText(String.valueOf(scoreP1));
        } else if (player == 2) {
            scoreP2 += points;
            if (scoreP2Label != null) scoreP2Label.setText(String.valueOf(scoreP2));
        }
    }

    @FXML
    private void onBackMenu() {
        if (timerTimeline != null) timerTimeline.stop();
        BomberManApp.showMenu();
    }
}