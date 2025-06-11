package BomberMan.controller;

import BomberMan.application.BomberManApp;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    @FXML private GridPane gridPane;
    @FXML private HBox topBar;
    @FXML private Label timerLabel;
    @FXML private Label scoreP1Label;
    @FXML private Label scoreP2Label;
    @FXML private Button backMenuButton;
    @FXML private ImageView p1Icon;
    @FXML private ImageView p2Icon;
    @FXML private HBox bottomBar;
    @FXML private Button btnPerso;
    @FXML private StackPane gameCenterPane;
    @FXML private Button btnRestartMusic;
    @FXML private ImageView restartIcon;
    @FXML private Button btnPauseMusic;
    @FXML private Button btnNextMusic;
    @FXML private ImageView pauseIcon;
    @FXML private ImageView nextIcon;

    private final int rows = 13, cols = 15;
    private final int cellSize = 40;

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
    private final String[] THEME_FOLDERS = {"asset_base", "asset_jungle", "asset_desert", "asset_backrooms"};
    private int themeIndex = 0;

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
    private Direction p1Dir = Direction.FACE;
    private int p1BombCount = 0;
    private int p1ExplosionRadius = 1;
    private int scoreP1 = 0;
    private boolean p1Alive = true;

    private int p2Row = rows - 2, p2Col = cols - 2;
    private Direction p2Dir = Direction.FACE;
    private int p2BombCount = 0;
    private int p2ExplosionRadius = 1;
    private int scoreP2 = 0;
    private boolean p2Alive = true;

    private Timeline timerTimeline;
    private int elapsedSeconds = 0;

    private boolean gameEnded = false;

    private Scene gameScene; // pour revenir à la scène de jeu d'origine

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

        if (p1Icon != null) p1Icon.setImage(getPlayerImage(COLORS[indexJ1], Direction.FACE));
        if (p2Icon != null) p2Icon.setImage(getPlayerImage(COLORS[indexJ2], Direction.FACE));

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
        if (btnNextMusic != null) {
            btnNextMusic.setOnAction(e -> playNextMusic());
        }
        
        // Gestion des boutons de contrôle musique avec MusicManager
        if (btnPauseMusic != null) {
            btnPauseMusic.setOnAction(e -> {
                if (mediaPlayer == null) return;
                if (isMusicPaused) {
                    mediaPlayer.play();
                    if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
                } else {
                    mediaPlayer.pause();
                    if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/play.png").toExternalForm()));
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
        gameCenterPane.widthProperty().addListener((obs, oldVal, newVal) -> resizeGridPane());
        gameCenterPane.heightProperty().addListener((obs, oldVal, newVal) -> resizeGridPane());
        resizeGridPane();
    }
    private boolean canHitPlayerWithBomb(int bombR, int bombC, int radius) {
        for (int[] dir : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            for (int dist = 1; dist <= radius; dist++) {
                int nr = bombR + dir[0]*dist, nc = bombC + dir[1]*dist;
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) break;
                if (map[nr][nc].equals("wall")) break;
                if (nr == p1Row && nc == p1Col && p1Alive) return true;
                if (map[nr][nc].equals("destructible")) break;
            }
        }
        return false;
    }

    @FXML
    private void onBtnPersoClick() {
        openPersonnalisationPage();
    }

    // Prédit si une bombe ici casserait un mur qui sépare l'IA du joueur
    private boolean canDestroyWallTowardsPlayer() {
        // Cherche direction du joueur
        int dr = Integer.compare(p1Row, p2Row);
        int dc = Integer.compare(p1Col, p2Col);
        // S'il y a un mur destructible entre l'IA et le joueur dans une ligne ou colonne
        if (dr == 0 || dc == 0) {
            int r = p2Row, c = p2Col;
            for (int i = 1; i <= p2ExplosionRadius; i++) {
                r += dr; c += dc;
                if (r < 0 || r >= rows || c < 0 || c >= cols) break;
                if (map[r][c].equals("wall")) break;
                if (map[r][c].equals("destructible")) return true;
                if (r == p1Row && c == p1Col) break;
            }
        }
        return false;
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
    private Direction getDirection(int fromR, int fromC, int toR, int toC) {
        if (fromR == toR) {
            if (toC == fromC + 1) return Direction.DROITE;
            if (toC == fromC - 1) return Direction.GAUCHE;
        }
        if (fromC == toC) {
            if (toR == fromR + 1) return Direction.DOS;
            if (toR == fromR - 1) return Direction.FACE;
        }
        return null;
    }
    public void initGame(boolean is1v1, String difficulty) {
        scoreP1 = 0;
        scoreP2 = 0;
        gameEnded = false;
        this.iaDifficulty = difficulty;
        isIaFacile = !is1v1 && "Facile".equalsIgnoreCase(difficulty);
        isIaNormal = !is1v1 && "Normal".equalsIgnoreCase(difficulty);
        isIaDifficile = !is1v1 && "Difficile".equalsIgnoreCase(difficulty);
        p1Row = 1; p1Col = 1; p1Dir = Direction.FACE; p1BombCount = 0; p1ExplosionRadius = 1; p1Alive = true;
        p2Row = rows - 2; p2Col = cols - 2; p2Dir = Direction.FACE; p2BombCount = 0; p2ExplosionRadius = 1; p2Alive = true;

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
            iaTimeline = new Timeline(new KeyFrame(Duration.seconds(1.2), e -> iaRandomMove()));
            iaTimeline.setCycleCount(Timeline.INDEFINITE);
            iaTimeline.play();
        }
        if (isIaNormal) {
            iaTimeline = new Timeline(new KeyFrame(Duration.seconds(0.4), e -> iaSmartMove()));
            iaTimeline.setCycleCount(Timeline.INDEFINITE);
            iaTimeline.play();
        }
        if (isIaDifficile) {
            iaTimeline = new Timeline(new KeyFrame(Duration.seconds(0.25), e -> iaDifficileMove()));
            iaTimeline.setCycleCount(Timeline.INDEFINITE);
            iaTimeline.play();
        }
        gridPane.requestFocus();
    }

    private void iaRandomMove() {
        if (!isIaFacile || gameEnded || !p2Alive) return;

        List<Direction> directions = Arrays.asList(Direction.DOS, Direction.FACE, Direction.GAUCHE, Direction.DROITE);
        Collections.shuffle(directions, aiRandom);

        // Essaye chaque direction aléatoirement, s'arrête dès que ça bouge
        for (Direction dir : directions) {
            int newRow = p2Row, newCol = p2Col;
            switch (dir) {
                case DOS:    newRow--; break;
                case FACE:  newRow++; break;
                case GAUCHE:  newCol--; break;
                case DROITE: newCol++; break;
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
            List<Direction> directions = Arrays.asList(Direction.FACE, Direction.DOS, Direction.GAUCHE, Direction.DROITE);
            Collections.shuffle(directions, aiRandom);

            boolean movedToSafe = false;
            // 1. Essayer d'aller sur une case walkable ET non dangereuse
            for (Direction dir : directions) {
                int newRow = p2Row, newCol = p2Col;
                switch (dir) {
                    case DOS:    newRow--; break;
                    case FACE:  newRow++; break;
                    case GAUCHE:  newCol--; break;
                    case DROITE: newCol++; break;
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
                        case DOS:    newRow--; break;
                        case FACE:  newRow++; break;
                        case GAUCHE:  newCol--; break;
                        case DROITE: newCol++; break;
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
            List<Direction> directions = Arrays.asList(Direction.FACE, Direction.DOS, Direction.GAUCHE, Direction.DROITE);
            Collections.shuffle(directions, aiRandom);

            for (Direction dir : directions) {
                int newRow = p2Row, newCol = p2Col;
                switch (dir) {
                    case DOS:    newRow--; break;
                    case FACE:  newRow++; break;
                    case GAUCHE:  newCol--; break;
                    case DROITE: newCol++; break;
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
    private void iaDifficileMove() {
        if (!isIaDifficile || gameEnded || !p2Alive) return;

        Set<String> dangerCells = getDangerCells();
        boolean inDanger = dangerCells.contains(p2Row + "," + p2Col);

        // --- 1. FUITE INTELLIGENTE SI EN DANGER ---
        if (inDanger) {
            // Cherche la case sûre la plus proche (BFS)
            boolean[][] visited = new boolean[rows][cols];
            Queue<int[]> queue = new ArrayDeque<>();
            queue.add(new int[]{p2Row, p2Col, 0});
            visited[p2Row][p2Col] = true;
            int[] goal = null;
            Map<String, int[]> parent = new HashMap<>();

            while (!queue.isEmpty()) {
                int[] cell = queue.poll();
                int r = cell[0], c = cell[1], dist = cell[2];
                if (!dangerCells.contains(r + "," + c) && isWalkable(r, c, 2)) {
                    goal = cell;
                    break;
                }
                for (int[] dir : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
                    int nr = r + dir[0], nc = c + dir[1];
                    if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                    if (!visited[nr][nc] && isWalkable(nr, nc, 2)) {
                        visited[nr][nc] = true;
                        queue.add(new int[]{nr, nc, dist + 1});
                        parent.put(nr + "," + nc, new int[]{r, c});
                    }
                }
            }
            if (goal != null && (goal[0] != p2Row || goal[1] != p2Col)) {
                // Revenir au premier pas vers la case safe
                int cr = goal[0], cc = goal[1];
                while (parent.containsKey(cr + "," + cc) && !(parent.get(cr + "," + cc)[0] == p2Row && parent.get(cr + "," + cc)[1] == p2Col)) {
                    int[] p = parent.get(cr + "," + cc);
                    cr = p[0];
                    cc = p[1];
                }
                Direction dir = getDirection(p2Row, p2Col, cr, cc);
                if (dir != null) {
                    p2Row = cr; p2Col = cc; p2Dir = dir;
                    updatePlayersDisplay();
                }
                return;
            }
            // Sinon, reste sur place (bloqué)
            return;
        }

        // --- 2. RAMASSE BONUS/MALUS ---
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

        // --- 3. SI JOUEUR ACCESSIBLE DIRECTEMENT : POSE UNE BOMBE ---
        if (canHitPlayerWithBomb(p2Row, p2Col, p2ExplosionRadius)) {
            if (bombs[p2Row][p2Col] == null && p2BombCount < 2 && !dangerCells.contains(p2Row + "," + p2Col)) {
                placeBomb(p2Row, p2Col, 2, p2ExplosionRadius);
                return;
            }
        }

        // --- 4. SI MUR DESTRUCTIBLE SEPARE IA ET JOUEUR : POSE UNE BOMBE ---
        if (canDestroyWallTowardsPlayer()) {
            if (bombs[p2Row][p2Col] == null && p2BombCount < 2 && !dangerCells.contains(p2Row + "," + p2Col)) {
                placeBomb(p2Row, p2Col, 2, p2ExplosionRadius);
                return;
            }
        }

        // --- 5. SE RAPPROCHER DU JOUEUR VIA LE PLUS COURT CHEMIN (EN EVITANT LES DANGERS) ---
        int[] target = {p1Row, p1Col};
        int[][] dist = new int[rows][cols];
        for (int[] row : dist) Arrays.fill(row, -1);
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{p2Row, p2Col});
        dist[p2Row][p2Col] = 0;
        Map<String, int[]> parent = new HashMap<>();
        int[] nextStep = null;
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int r = cell[0], c = cell[1];
            if (r == target[0] && c == target[1]) {
                // On a trouvé un chemin jusqu'au joueur
                nextStep = cell;
                break;
            }
            for (int[] dir : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (dist[nr][nc] == -1 && isWalkable(nr, nc, 2) && !dangerCells.contains(nr + "," + nc)) {
                    dist[nr][nc] = dist[r][c] + 1;
                    queue.add(new int[]{nr, nc});
                    parent.put(nr + "," + nc, new int[]{r, c});
                }
            }
        }
        if (nextStep != null) {
            // Revenir au premier pas
            int cr = nextStep[0], cc = nextStep[1];
            while (parent.containsKey(cr + "," + cc) && !(parent.get(cr + "," + cc)[0] == p2Row && parent.get(cr + "," + cc)[1] == p2Col)) {
                int[] p = parent.get(cr + "," + cc);
                cr = p[0];
                cc = p[1];
            }
            Direction dir = getDirection(p2Row, p2Col, cr, cc);
            if (dir != null) {
                p2Row = cr; p2Col = cc; p2Dir = dir;
                updatePlayersDisplay();
                return;
            }
        }

        // --- 6. Si pas d'action possible, casser des murs pour explorer ---
        boolean foundDestructible = false;
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue2 = new ArrayDeque<>();
        queue2.add(new int[]{p2Row, p2Col});
        visited[p2Row][p2Col] = true;
        Map<String, int[]> parent2 = new HashMap<>();
        int[] destructibleCell = null;

        while (!queue2.isEmpty() && !foundDestructible) {
            int[] cell = queue2.poll();
            int r = cell[0], c = cell[1];
            // Cherche autour si un mur destructible est à portée de bombe
            for (int[] dir : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
                for (int dist2 = 1; dist2 <= p2ExplosionRadius; dist2++) {
                    int nr = r + dir[0]*dist2, nc = c + dir[1]*dist2;
                    if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) break;
                    if (map[nr][nc].equals("wall")) break;
                    if (map[nr][nc].equals("destructible")) {
                        // Si IA est déjà à la bonne position, pose une bombe !
                        if (r == p2Row && c == p2Col && bombs[p2Row][p2Col] == null && p2BombCount < 2) {
                            placeBomb(p2Row, p2Col, 2, p2ExplosionRadius);
                            return;
                        }
                        // Sinon, on va essayer d'atteindre ce point
                        destructibleCell = cell;
                        foundDestructible = true;
                        break;
                    }
                }
                if (foundDestructible) break;
            }
            // Parcours en largeur pour atteindre une case d'où l'on pourra casser un mur
            for (int[] dir : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (!visited[nr][nc] && isWalkable(nr, nc, 2)) {
                    visited[nr][nc] = true;
                    queue2.add(new int[]{nr, nc});
                    parent2.put(nr + "," + nc, new int[]{r, c});
                }
            }
        }
        // Si on a trouvé une case d'où casser un mur, on y va
        if (destructibleCell != null && (destructibleCell[0] != p2Row || destructibleCell[1] != p2Col)) {
            int cr = destructibleCell[0], cc = destructibleCell[1];
            while (parent2.containsKey(cr + "," + cc) && !(parent2.get(cr + "," + cc)[0] == p2Row && parent2.get(cr + "," + cc)[1] == p2Col)) {
                int[] p = parent2.get(cr + "," + cc);
                cr = p[0];
                cc = p[1];
            }
            Direction dir = getDirection(p2Row, p2Col, cr, cc);
            if (dir != null) {
                p2Row = cr; p2Col = cc; p2Dir = dir;
                updatePlayersDisplay();
                return;
            }
        }

        // --- 7. SINON, DEPLACEMENT RANDOM ---
        List<Direction> directions = Arrays.asList(Direction.FACE, Direction.DOS, Direction.GAUCHE, Direction.DROITE);
        Collections.shuffle(directions, aiRandom);
        for (Direction dir : directions) {
            int newRow = p2Row, newCol = p2Col;
            switch (dir) {
                case FACE:    newRow--; break;
                case DOS:  newRow++; break;
                case GAUCHE:  newCol--; break;
                case DROITE: newCol++; break;
            }
            if (isWalkable(newRow, newCol, 2) && !dangerCells.contains(newRow + "," + newCol)) {
                p2Row = newRow;
                p2Col = newCol;
                p2Dir = dir;
                updatePlayersDisplay();
                return;
            }
        }
        // Sinon, reste sur place
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
            case DOS:    suffix = "Dos"; break;
            case GAUCHE:  suffix = "Gauche"; break;
            case DROITE: suffix = "Droite"; break;
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
            case Z: newRow1--; newDir1 = Direction.DOS; moved1 = true; break;
            case S: newRow1++; newDir1 = Direction.FACE; moved1 = true; break;
            case Q: newCol1--; newDir1 = Direction.GAUCHE; moved1 = true; break;
            case D: newCol1++; newDir1 = Direction.DROITE; moved1 = true; break;
            case E: bomb1 = true; break;
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
            case I: newRow2--; newDir2 = Direction.DOS; moved2 = true; break;
            case K: newRow2++; newDir2 = Direction.FACE; moved2 = true; break;
            case J: newCol2--; newDir2 = Direction.GAUCHE; moved2 = true; break;
            case L: newCol2++; newDir2 = Direction.DROITE; moved2 = true; break;
            case U: bomb2 = true; break;
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
        p1Row = 1; p1Col = 1; p1Dir = Direction.FACE; p1BombCount = 0; p1ExplosionRadius = 1; p1Alive = true;
        p2Row = rows - 2; p2Col = cols - 2; p2Dir = Direction.FACE; p2BombCount = 0; p2ExplosionRadius = 1; p2Alive = true;
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
            winnerImg = new ImageView(getPlayerImage(COLORS[indexJ1], Direction.FACE));
        } else {
            winnerImg = new ImageView(getPlayerImage(COLORS[indexJ2], Direction.FACE));
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Personnalisation.fxml"));
            Parent root = loader.load();
            PersonnalisationController persoController = loader.getController();
            Stage stage = (Stage) gridPane.getScene().getWindow();

            persoController.setContext(
                    stage,
                    gridPane.getScene(),
                    (newJ1, newJ2, newTheme) -> {
                        this.indexJ1 = newJ1;
                        this.indexJ2 = newJ2;
                        this.themeIndex = newTheme;
                        loadThemeAssets();
                        drawBoard();
                        updatePlayersDisplay();
                        if (p1Icon != null) p1Icon.setImage(getPlayerImage(COLORS[indexJ1], Direction.FACE));
                        if (p2Icon != null) p2Icon.setImage(getPlayerImage(COLORS[indexJ2], Direction.FACE));
                    },
                    this.indexJ1, this.indexJ2, this.themeIndex
            );

            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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