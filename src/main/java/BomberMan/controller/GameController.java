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
import javafx.scene.media.AudioClip;

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
    @FXML
    private StackPane gameCenterPane;

    private final int rows = 13, cols = 15;

    private String[][] map = new String[rows][cols];
    private Bomb[][] bombs = new Bomb[rows][cols];
    private boolean[][] isExplosion = new boolean[rows][cols];

    private Image bonusImg;
    private ImageView[][] cellBonuses = new ImageView[rows][cols];

    // Personnalisation couleurs des joueurs
    private final String[] COLORS = {"Blanc", "Rouge", "Bleu", "Noir", "Jacob"};
    private int indexJ1 = 0;
    private int indexJ2 = 1;

    // Personnalisation thème de la map
    private final String[] THEMES = {"Base", "Jungle", "Désert", "Backrooms"};
    private final String[] THEME_FOLDERS = {"asset_base", "asset_jungle", "asset_desert", "backrooms_asset"};
    private int themeIndex = 0; // 0: base, 1: jungle, 2: desert, 3: backrooms

    private Image pelouseImg, wallImg, destructibleImg, bombImg, explosionImg;
    private ImageView[][] cellBackgrounds = new ImageView[rows][cols];
    private ImageView[][] cellExplosions  = new ImageView[rows][cols];
    private ImageView[][] cellBombs       = new ImageView[rows][cols];
    private ImageView[][] cellPlayer1     = new ImageView[rows][cols];
    private ImageView[][] cellPlayer2     = new ImageView[rows][cols];

    //son bonus
    private AudioClip bonusSound;

    //ia
    private String iaDifficulty = null;
    private boolean isIaFacile = false;
    private boolean isIaNormal = false;
    private boolean isIaDifficile = false;
    private Random aiRandom = new Random();
    private Timeline iaTimeline;
    private int iaBombCooldown = 0;


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
        setupGridPaneResize();
        
        if (p1Icon != null) p1Icon.setImage(getPlayerImage(COLORS[indexJ1], Direction.DOWN));
        if (p2Icon != null) p2Icon.setImage(getPlayerImage(COLORS[indexJ2], Direction.DOWN));
        
        gridPane.getChildren().clear();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                StackPane cell = new StackPane();
                cell.setMinSize(0, 0);
                cell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                
                ImageView bg    = new ImageView();
                ImageView expl  = new ImageView();
                ImageView bomb  = new ImageView();
                ImageView p1    = new ImageView();
                ImageView p2    = new ImageView();
                ImageView bonus = new ImageView();
                
                for (ImageView img : new ImageView[]{bg, expl, bomb, p1, p2, bonus}) {
                    img.fitWidthProperty().bind(cell.widthProperty());
                    img.fitHeightProperty().bind(cell.heightProperty());
                    img.setPreserveRatio(true);
                }
                
                expl.setVisible(false);
                bomb.setVisible(false);
                p1.setVisible(false);
                p2.setVisible(false);
                bonus.setVisible(false);
                
                cellBackgrounds[r][c] = bg;
                cellExplosions[r][c]  = expl;
                cellBombs[r][c]       = bomb;
                cellPlayer1[r][c]     = p1;
                cellPlayer2[r][c]     = p2;
                cellBonuses[r][c]     = bonus;
                
                cell.getChildren().addAll(bg, bonus, expl, bomb, p1, p2);
                
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
        
        URL bonusSoundUrl = getClass().getResource("/Son/Bonus.mp3");
        if (bonusSoundUrl != null) {
            bonusSound = new AudioClip(bonusSoundUrl.toExternalForm());
        }
        
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
        
        // Initialisation des icônes des boutons
        if (pauseIcon != null) {
            pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
        }
        if (nextIcon != null) {
            nextIcon.setImage(new Image(getClass().getResource("/images/pass.png").toExternalForm()));
        }
        if (restartIcon != null) {
            restartIcon.setImage(new Image(getClass().getResource("/images/revenir.png").toExternalForm()));
        }
        
        // Gestion des boutons de contrôle musique avec MusicManager
        if (btnPauseMusic != null) {
            btnPauseMusic.setOnAction(e -> {
                if (MusicManager.isPaused()) {
                    MusicManager.resume();
                    if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
                } else {
                    MusicManager.pause();
                    if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/play.png").toExternalForm()));
                }
            });
        }
        if (btnNextMusic != null) {
            btnNextMusic.setOnAction(e -> {
                MusicManager.playNextGameMusic(); // à implémenter dans MusicManager pour changer de musique de fond du jeu
                if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
            });
        }
        if (btnRestartMusic != null) {
            btnRestartMusic.setOnAction(e -> {
                MusicManager.restart();
                if (!MusicManager.isPaused() && pauseIcon != null)
                    pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
            });
        }
        
        // Lance la musique du jeu à l'arrivée sur l'écran jeu
        MusicManager.playGameMusic();
        
        gameCenterPane.widthProperty().addListener((obs, oldVal, newVal) -> resizeGridPane());
        gameCenterPane.heightProperty().addListener((obs, oldVal, newVal) -> resizeGridPane());
        resizeGridPane();
    }

    private void resizeGridPane() {
        double paneW = gameCenterPane.getWidth();
        double paneH = gameCenterPane.getHeight();

        // Respecte le ratio cols/rows, cases carrées
        double cellSize = Math.min(paneW / cols, paneH / rows);

        gridPane.setPrefWidth(cellSize * cols);
        gridPane.setPrefHeight(cellSize * rows);
        gridPane.setMaxWidth(cellSize * cols);
        gridPane.setMaxHeight(cellSize * rows);
    }

    private void setupGridPaneResize() {
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();
        for (int c = 0; c < cols; c++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / cols);
            colConst.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(colConst);
        }
        for (int r = 0; r < rows; r++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / rows);
            rowConst.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(rowConst);
        }
    }

    private void loadThemeAssets() {
        String folder = THEME_FOLDERS[themeIndex];
        pelouseImg      = loadAsset(folder, "pelouse.png");
        wallImg         = loadAsset(folder, "wall.png");
        destructibleImg = loadAsset(folder, "destructible.png");
        bombImg         = loadAsset(folder, "bomb.png");
        explosionImg    = loadAsset(folder, "explosion.png");
        bonusImg = loadAsset(folder, "bonus.png");
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

    public void initGame(boolean is1v1, String difficulty) {
        scoreP1 = 0;
        scoreP2 = 0;
        gameEnded = false;
        this.iaDifficulty = difficulty;
        isIaFacile = !is1v1 && "Facile".equalsIgnoreCase(difficulty);
        isIaNormal = !is1v1 && "Normal".equalsIgnoreCase(difficulty);
        isIaDifficile = !is1v1 && "Difficile".equalsIgnoreCase(difficulty);
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

        if (iaTimeline != null) iaTimeline.stop();

        if (isIaFacile) {
            iaTimeline = new Timeline(new KeyFrame(Duration.seconds(1.2), e -> iaRandomMove())); // <- ralentis ici
            iaTimeline.setCycleCount(Timeline.INDEFINITE);
            iaTimeline.play();
        }
        if (isIaNormal) {
            iaTimeline = new Timeline(new KeyFrame(Duration.seconds(0.4), e -> iaSmartMove())); // même lenteur, tu peux ajuster
            iaTimeline.setCycleCount(Timeline.INDEFINITE);
            iaTimeline.play();
        }
        gridPane.requestFocus();
    }

    private void iaRandomMove() {
        if (!isIaFacile || gameEnded || !p2Alive) return;

        List<Direction> directions = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
        Collections.shuffle(directions, aiRandom);

        // Essaye chaque direction aléatoirement, s'arrête dès que ça bouge
        for (Direction dir : directions) {
            int newRow = p2Row, newCol = p2Col;
            switch (dir) {
                case UP:    newRow--; break;
                case DOWN:  newRow++; break;
                case LEFT:  newCol--; break;
                case RIGHT: newCol++; break;
            }
            if (isWalkable(newRow, newCol, 2)) {
                p2Row = newRow;
                p2Col = newCol;
                p2Dir = dir;
                updatePlayersDisplay();
                // Ramasse bonus/malus si présent
                if (map[p2Row][p2Col].equals("bonus_range")) {
                    p2ExplosionRadius = Math.min(p2ExplosionRadius + 1, 10);
                    if (bonusSound != null) bonusSound.play();
                    map[p2Row][p2Col] = "pelouse";
                    updateBonusesDisplay();
                } else if (map[p2Row][p2Col].equals("malus_range")) {
                    p2ExplosionRadius = Math.max(p2ExplosionRadius - 1, 1);
                    map[p2Row][p2Col] = "pelouse";
                    updateBonusesDisplay();
                }
                break;
            }
        }
        // Petite chance de poser une bombe
        if (aiRandom.nextDouble() < 0.25) {
            placeBomb(p2Row, p2Col, 2, p2ExplosionRadius);
        }
    }
    private Set<String> getDangerCells() {
        Set<String> dangerCells = new HashSet<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Bomb bomb = bombs[r][c];
                if (bomb != null) {
                    // Ajoute la case de la bombe
                    dangerCells.add(r + "," + c);
                    // Ajoute les cases touchées par l'explosion
                    for (int[] dir : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
                        for (int dist = 1; dist <= bomb.radius; dist++) {
                            int nr = r + dir[0]*dist, nc = c + dir[1]*dist;
                            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) break;
                            if (map[nr][nc].equals("wall")) break;
                            dangerCells.add(nr + "," + nc);
                            if (map[nr][nc].equals("destructible")) break;
                        }
                    }
                }
            }
        }
        return dangerCells;
    }
    private void iaSmartMove() {
        if (!isIaNormal || gameEnded || !p2Alive) return;

        Set<String> dangerCells = getDangerCells();
        boolean inDanger = dangerCells.contains(p2Row + "," + p2Col);

        if (inDanger) {
            // Cherche toutes les directions où fuir
            List<Direction> directions = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
            Collections.shuffle(directions, aiRandom);

            boolean movedToSafe = false;
            // 1. Essayer d'aller sur une case walkable ET non dangereuse
            for (Direction dir : directions) {
                int newRow = p2Row, newCol = p2Col;
                switch (dir) {
                    case UP:    newRow--; break;
                    case DOWN:  newRow++; break;
                    case LEFT:  newCol--; break;
                    case RIGHT: newCol++; break;
                }
                if (isWalkable(newRow, newCol, 2) && !dangerCells.contains(newRow + "," + newCol)) {
                    p2Row = newRow;
                    p2Col = newCol;
                    p2Dir = dir;
                    updatePlayersDisplay();
                    movedToSafe = true;
                    break;
                }
            }
            // 2. Si aucune case safe dispo, bouger vers n'importe quelle case walkable (même si dangereuse)
            if (!movedToSafe) {
                for (Direction dir : directions) {
                    int newRow = p2Row, newCol = p2Col;
                    switch (dir) {
                        case UP:    newRow--; break;
                        case DOWN:  newRow++; break;
                        case LEFT:  newCol--; break;
                        case RIGHT: newCol++; break;
                    }
                    if (isWalkable(newRow, newCol, 2)) {
                        p2Row = newRow;
                        p2Col = newCol;
                        p2Dir = dir;
                        updatePlayersDisplay();
                        break;
                    }
                }
            }
        } else {
            // S'il n'est pas en danger, déplacement aléatoire
            List<Direction> directions = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
            Collections.shuffle(directions, aiRandom);

            for (Direction dir : directions) {
                int newRow = p2Row, newCol = p2Col;
                switch (dir) {
                    case UP:    newRow--; break;
                    case DOWN:  newRow++; break;
                    case LEFT:  newCol--; break;
                    case RIGHT: newCol++; break;
                }
                if (isWalkable(newRow, newCol, 2)) {
                    p2Row = newRow;
                    p2Col = newCol;
                    p2Dir = dir;
                    updatePlayersDisplay();
                    break;
                }
            }
        }

        // Ramasse bonus/malus si présent
        if (map[p2Row][p2Col].equals("bonus_range")) {
            p2ExplosionRadius = Math.min(p2ExplosionRadius + 1, 10);
            if (bonusSound != null) bonusSound.play();
            map[p2Row][p2Col] = "pelouse";
            updateBonusesDisplay();
        } else if (map[p2Row][p2Col].equals("malus_range")) {
            p2ExplosionRadius = Math.max(p2ExplosionRadius - 1, 1);
            map[p2Row][p2Col] = "pelouse";
            updateBonusesDisplay();
        }

        // Petite chance de poser une bombe, mais NE POSE PAS si tu es déjà sur une bombe !
        if (aiRandom.nextDouble() < 0.25 && bombs[p2Row][p2Col] == null) {
            placeBomb(p2Row, p2Col, 2, p2ExplosionRadius);
        }
        if (iaBombCooldown > 0) iaBombCooldown--;

// ... et pour poser une bombe :
        if (iaBombCooldown == 0 && aiRandom.nextDouble() < 0.25 && bombs[p2Row][p2Col] == null && !getDangerCells().contains(p2Row + "," + p2Col)) {
            placeBomb(p2Row, p2Col, 2, p2ExplosionRadius);
            iaBombCooldown = 10; // par exemple, 8 cycles de Timeline (si Timeline à 0.1s, ça fait 0.8s)
        }
    }
    private void updateBonusesDisplay() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                cellBonuses[r][c].setVisible(false);
                if (map[r][c].equals("bonus_range") || map[r][c].equals("malus_range")) {
                    cellBonuses[r][c].setImage(bonusImg);
                    cellBonuses[r][c].setVisible(true);
                }
            }
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
            cellPlayer1[p1Row][p1Col].setImage(getPlayerImage(COLORS[indexJ1], p1Dir));
            cellPlayer1[p1Row][p1Col].setVisible(true);
        }
        if (p2Alive && !map[p2Row][p2Col].equals("wall") && !map[p2Row][p2Col].equals("destructible")) {
            cellPlayer2[p2Row][p2Col].setImage(getPlayerImage(COLORS[indexJ2], p2Dir));
            cellPlayer2[p2Row][p2Col].setVisible(true);
        }
    }

    private Image getPlayerImage(String colorKey, Direction dir) {
        String suffix;
        switch (dir) {
            case UP:    suffix = "Dos"; break;
            case LEFT:  suffix = "Gauche"; break;
            case RIGHT: suffix = "Droite"; break;
            default:    suffix = "Face"; break;
        }
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
            case Z:
                newRow1--;
                newDir1 = Direction.UP;
                moved1 = true;
                break;
            case S:
                newRow1++;
                newDir1 = Direction.DOWN;
                moved1 = true;
                break;
            case Q:
                newCol1--;
                newDir1 = Direction.LEFT;
                moved1 = true;
                break;
            case D:
                newCol1++;
                newDir1 = Direction.RIGHT;
                moved1 = true;
                break;
            case E:
                bomb1 = true;
                break;
        }
        if (moved1 && p1Alive) {
            if (isWalkable(newRow1, newCol1, 1)) {
                p1Row = newRow1;
                p1Col = newCol1;
                if (map[p1Row][p1Col].equals("bonus_range")) {
                    p1ExplosionRadius = Math.min(p1ExplosionRadius + 1, 10);
                    if (bonusSound != null) bonusSound.play();
                } else if (map[p1Row][p1Col].equals("malus_range")) {
                    p1ExplosionRadius = Math.max(p1ExplosionRadius - 1, 1);
                }
                if (map[p1Row][p1Col].equals("bonus_range") || map[p1Row][p1Col].equals("malus_range")) {
                    map[p1Row][p1Col] = "pelouse";
                    updateBonusesDisplay();
                }
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
            case I:
                newRow2--;
                newDir2 = Direction.UP;
                moved2 = true;
                break;
            case K:
                newRow2++;
                newDir2 = Direction.DOWN;
                moved2 = true;
                break;
            case J:
                newCol2--;
                newDir2 = Direction.LEFT;
                moved2 = true;
                break;
            case L:
                newCol2++;
                newDir2 = Direction.RIGHT;
                moved2 = true;
                break;
            case U:
                bomb2 = true;
                break;
        }
        if (moved2 && p2Alive) {
            if (isWalkable(newRow2, newCol2, 2)) {
                p2Row = newRow2;
                p2Col = newCol2;
                if (map[p2Row][p2Col].equals("bonus_range")) {
                    p2ExplosionRadius = Math.min(p2ExplosionRadius + 1, 10);
                    if (bonusSound != null) bonusSound.play();
                } else if (map[p2Row][p2Col].equals("malus_range")) {
                    p2ExplosionRadius = Math.max(p2ExplosionRadius - 1, 1);
                }
                if (map[p2Row][p2Col].equals("bonus_range") || map[p2Row][p2Col].equals("malus_range")) {
                    map[p2Row][p2Col] = "pelouse";
                    updateBonusesDisplay();
                }
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
        if (!(map[row][col].equals("pelouse") || map[row][col].equals("bonus_range") || map[row][col].equals("malus_range"))) return false;
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
                double rand = Math.random();
                if (rand < 0.15) {
                    map[r][c] = "bonus_range";
                } else if (rand < 0.25) {
                    map[r][c] = "malus_range";
                } else {
                    map[r][c] = "pelouse";
                }
            }
            if (bombs[r][c] != null) explodeBomb(r, c);
        }
        drawBoard();
        updateBonusesDisplay();

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
        p1ExplosionRadius = 1;
        p2ExplosionRadius = 1;

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                bombs[r][c] = null;
                isExplosion[r][c] = false;
            }
        generateRandomMap();
        updateBonusesDisplay();
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
            winnerImg = new ImageView(getPlayerImage(COLORS[indexJ1], Direction.DOWN));
        } else {
            winnerImg = new ImageView(getPlayerImage(COLORS[indexJ2], Direction.DOWN));
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
        ImageView imageJ1 = new ImageView(getPlayerImage(COLORS[tempIndexJ1[0]], Direction.DOWN));
        imageJ1.setFitWidth(52); imageJ1.setFitHeight(52); imageJ1.setPreserveRatio(true);
        leftJ1.setOnAction(e -> {
            tempIndexJ1[0] = (tempIndexJ1[0] - 1 + COLORS.length) % COLORS.length;
            couleurJ1Label.setText(COLORS[tempIndexJ1[0]]);
            imageJ1.setImage(getPlayerImage(COLORS[tempIndexJ1[0]], Direction.DOWN));
        });
        rightJ1.setOnAction(e -> {
            tempIndexJ1[0] = (tempIndexJ1[0] + 1) % COLORS.length;
            couleurJ1Label.setText(COLORS[tempIndexJ1[0]]);
            imageJ1.setImage(getPlayerImage(COLORS[tempIndexJ1[0]], Direction.DOWN));
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
        ImageView imageJ2 = new ImageView(getPlayerImage(COLORS[tempIndexJ2[0]], Direction.DOWN));
        imageJ2.setFitWidth(52); imageJ2.setFitHeight(52); imageJ2.setPreserveRatio(true);
        leftJ2.setOnAction(e -> {
            tempIndexJ2[0] = (tempIndexJ2[0] - 1 + COLORS.length) % COLORS.length;
            couleurJ2Label.setText(COLORS[tempIndexJ2[0]]);
            imageJ2.setImage(getPlayerImage(COLORS[tempIndexJ2[0]], Direction.DOWN));
        });
        rightJ2.setOnAction(e -> {
            tempIndexJ2[0] = (tempIndexJ2[0] + 1) % COLORS.length;
            couleurJ2Label.setText(COLORS[tempIndexJ2[0]]);
            imageJ2.setImage(getPlayerImage(COLORS[tempIndexJ2[0]], Direction.DOWN));
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
            if (p1Icon != null) p1Icon.setImage(getPlayerImage(COLORS[indexJ1], Direction.DOWN));
            if (p2Icon != null) p2Icon.setImage(getPlayerImage(COLORS[indexJ2], Direction.DOWN));
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