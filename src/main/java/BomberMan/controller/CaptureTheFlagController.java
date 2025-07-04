package BomberMan.controller;

import BomberMan.Direction;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.awt.Point;
/**
 * Contrôleur du mode "Capture The Flag" du jeu BomberMan.
 * Gère l'affichage, la logique de jeu, les déplacements, la gestion des bombes, du score, du timer, des flags et des boutons musicaux.
 */
public class CaptureTheFlagController {

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
    @FXML private Label winnerLabel;

    private final int rows = 13, cols = 15;
    private String[][] map = new String[rows][cols];
    private Image pelouseImg, wallImg, destructibleImg, flagImg, bombImg, explosionImg;
    private ImageView[][] cellBackgrounds = new ImageView[rows][cols];
    private ImageView[][] cellDestructible = new ImageView[rows][cols];
    private ImageView[][] cellFlags = new ImageView[rows][cols];
    private ImageView[][] cellPlayer1 = new ImageView[rows][cols];
    private ImageView[][] cellPlayer2 = new ImageView[rows][cols];
    private ImageView[][] cellBombs = new ImageView[rows][cols];
    private ImageView[][] cellExplosions = new ImageView[rows][cols];

    private final String[] COLORS = {"Blanc", "Rouge", "Bleu", "Noir", "Jacob"};
    private final String[] THEME_FOLDERS = {"asset_base", "asset_jungle", "asset_desert", "asset_backrooms"};
    private int indexJ1 = 0;
    private int indexJ2 = 1;
    private int themeIndex = 0;
    private int scoreP1 = 0, scoreP2 = 0;
    private int elapsedSeconds = 0;
    private Timeline timerTimeline;
    private boolean gameEnded = false;

    private Point p1Base = new Point(1, 1);
    private Point p2Base = new Point(rows-2, cols-2);
    private Point p1Pos, p2Pos;
    private Point flag1Pos, flag2Pos;
    private boolean p1HasFlag = false, p2HasFlag = false;
    private final int maxBombsPerPlayer = 2;
    private final int bombRadius = 2;
    private int p1BombCount = 0;
    private int p2BombCount = 0;
    private Bomb[][] bombs = new Bomb[rows][cols];
    private boolean[][] isExplosion = new boolean[rows][cols];
    private Scene gameScene;

    private Direction p1Direction = Direction.FACE;
    private Direction p2Direction = Direction.FACE;

    private static class Bomb {
        int row, col, owner, radius;
        public Bomb(int row, int col, int owner, int radius) {
            this.row = row; this.col = col; this.owner = owner; this.radius = radius;
        }
    }

    /**
     * Initialise la scène CTF :
     * - Charge les thèmes et images
     * - Génère la grille de jeu, les joueurs, flags et murs
     * - Met à jour le timer, les scores, l'affichage
     * - Branche les boutons (menu, personnalisation, musique)
     * - Démarre la musique de jeu
     */
    @FXML
    public void initialize() {
        loadThemeAssets();

        p1Pos = new Point(p1Base);
        p2Pos = new Point(p2Base);
        flag1Pos = new Point(p1Base);
        flag2Pos = new Point(p2Base);

        generateCTFMap();

        if (p1Icon != null) p1Icon.setImage(getPlayerImage(COLORS[indexJ1], Direction.FACE));
        if (p2Icon != null) p2Icon.setImage(getPlayerImage(COLORS[indexJ2], Direction.FACE));

        setupGridPaneResize();
        gridPane.getChildren().clear();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                StackPane cell = new StackPane();
                cell.setMinSize(0, 0);
                cell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                ImageView bg    = new ImageView();
                ImageView destructible = new ImageView();
                ImageView flag  = new ImageView();
                ImageView p1    = new ImageView();
                ImageView p2    = new ImageView();
                ImageView bomb  = new ImageView();
                ImageView explosion = new ImageView();

                for (ImageView img : new ImageView[]{bg, destructible, flag, p1, p2, bomb, explosion}) {
                    img.fitWidthProperty().bind(cell.widthProperty());
                    img.fitHeightProperty().bind(cell.heightProperty());
                    img.setPreserveRatio(true);
                }
                destructible.setVisible(false);
                flag.setVisible(false);
                p1.setVisible(false);
                p2.setVisible(false);
                bomb.setVisible(false);
                explosion.setVisible(false);

                cellBackgrounds[r][c] = bg;
                cellDestructible[r][c] = destructible;
                cellFlags[r][c] = flag;
                cellPlayer1[r][c] = p1;
                cellPlayer2[r][c] = p2;
                cellBombs[r][c] = bomb;
                cellExplosions[r][c] = explosion;
                
                cell.getChildren().addAll(bg, destructible, flag, p1, p2, bomb, explosion);
                
                gridPane.add(cell, c, r);
            }
        }
        drawBoard();
        updatePlayersDisplay();
        updateFlags();
        updateBombs();
        updateExplosions();


        if (timerLabel != null) timerLabel.setText("00:00");
        if (scoreP1Label != null) scoreP1Label.setText("0");
        if (scoreP2Label != null) scoreP2Label.setText("0");
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.playFromStart();


        gridPane.setFocusTraversable(true);
        gridPane.requestFocus();
        gridPane.setOnKeyPressed(this::handleKeyPressed);
        gridPane.setOnMouseClicked(e -> gridPane.requestFocus());


        if (backMenuButton != null) backMenuButton.setOnAction(e -> onBackMenu());
        if (btnPerso != null) btnPerso.setOnAction(e -> openPersonnalisationPage());


        if (pauseIcon != null) pauseIcon.setImage(safeLoad("/images/pause.png"));
        if (nextIcon != null) nextIcon.setImage(safeLoad("/images/pass.png"));
        if (restartIcon != null) restartIcon.setImage(safeLoad("/images/revenir.png"));


        gameCenterPane.widthProperty().addListener((obs, oldVal, newVal) -> resizeGridPane());
        gameCenterPane.heightProperty().addListener((obs, oldVal, newVal) -> resizeGridPane());
        resizeGridPane();
        
        if (winnerLabel != null) winnerLabel.setVisible(false);
        
        if (btnPauseMusic != null) btnPauseMusic.setOnAction(e -> onPauseMusic());
        if (btnNextMusic != null) btnNextMusic.setOnAction(e -> onNextMusic());
        if (btnRestartMusic != null) btnRestartMusic.setOnAction(e -> onRestartMusic());

        if (pauseIcon != null) {
            pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
        }
        if (nextIcon != null) {
            nextIcon.setImage(new Image(getClass().getResource("/images/pass.png").toExternalForm()));
        }
        if (restartIcon != null) {
            restartIcon.setImage(new Image(getClass().getResource("/images/revenir.png").toExternalForm()));
        }

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
        if (btnRestartMusic != null) {
            btnRestartMusic.setOnAction(e -> {
                MusicManager.restart();
                if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
            });
        }
        if (btnNextMusic != null) {
            btnNextMusic.setOnAction(e -> {
                MusicManager.playNextGameMusic();
                if (pauseIcon != null) pauseIcon.setImage(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
            });
        }

        MusicManager.playGameMusic();
    }

    /**
     * Charge les assets (images) selon le thème choisi.
     * Appelle loadAsset pour chaque image nécessaire au plateau.
     */
    private void loadThemeAssets() {
        String folder = THEME_FOLDERS[themeIndex];
        pelouseImg      = loadAsset(folder, "pelouse.png");
        wallImg         = loadAsset(folder, "wall.png");
        destructibleImg = loadAsset(folder, "destructible.png");
        bombImg         = loadAsset(folder, "bomb.png");
        explosionImg    = loadAsset(folder, "explosion.png");
        flagImg         = safeLoad("/images/flag.png");
    }

    /**
     * Génère aléatoirement la carte pour le mode CTF :
     * - Place des murs indestructibles, des murs destructibles, de la pelouse
     * - Libère les bases et les flags
     */
    private void generateCTFMap() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1 || (r % 2 == 0 && c % 2 == 0)) {
                    map[r][c] = "wall";
                } else {
                    boolean nearBase = (Math.abs(r - p1Base.x) <= 1 && Math.abs(c - p1Base.y) <= 1)
                            || (Math.abs(r - p2Base.x) <= 1 && Math.abs(c - p2Base.y) <= 1);
                    map[r][c] = (!nearBase && Math.random() < 0.45) ? "destructible" : "pelouse";
                }
            }
        }
        map[p1Base.x][p1Base.y] = "pelouse";
        map[p2Base.x][p2Base.y] = "pelouse";
        map[flag1Pos.x][flag1Pos.y] = "pelouse";
        map[flag2Pos.x][flag2Pos.y] = "pelouse";
    }

    /**
     * Met à jour l'affichage du plateau (fond, murs, pelouse, destructibles).
     */
    private void drawBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cellBackgrounds[r][c].setImage(map[r][c].equals("wall") ? wallImg : pelouseImg);
                if (map[r][c].equals("destructible")) {
                    cellDestructible[r][c].setImage(destructibleImg);
                    cellDestructible[r][c].setVisible(true);
                } else {
                    cellDestructible[r][c].setVisible(false);
                }
            }
        }
    }

    /**
     * Met à jour l'affichage des joueurs sur la grille.
     */
    private void updatePlayersDisplay() {
        for (int r=0; r<rows; r++) for (int c=0; c<cols; c++) {
            cellPlayer1[r][c].setVisible(false);
            cellPlayer2[r][c].setVisible(false);
        }
        cellPlayer1[p1Pos.x][p1Pos.y].setImage(getPlayerImage(COLORS[indexJ1], p1Direction));
        cellPlayer1[p1Pos.x][p1Pos.y].setVisible(true);
        cellPlayer2[p2Pos.x][p2Pos.y].setImage(getPlayerImage(COLORS[indexJ2], p2Direction));
        cellPlayer2[p2Pos.x][p2Pos.y].setVisible(true);
    }

    /**
     * Met à jour la position et l'affichage des flags.
     */
    private void updateFlags() {
        for (int r=0; r<rows; r++) for (int c=0; c<cols; c++) {
            cellFlags[r][c].setVisible(false);
            cellFlags[r][c].setImage(null);
        }
        if (!p2HasFlag && inMap(flag2Pos) && flagImg != null) {
            cellFlags[flag2Pos.x][flag2Pos.y].setImage(flagImg);
            cellFlags[flag2Pos.x][flag2Pos.y].setVisible(true);
        }
        if (!p1HasFlag && inMap(flag1Pos) && flagImg != null) {
            cellFlags[flag1Pos.x][flag1Pos.y].setImage(flagImg);
            cellFlags[flag1Pos.x][flag1Pos.y].setVisible(true);
        }
    }

    /**
     * Met à jour l'affichage des bombes sur la grille.
     */
    private void updateBombs() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                boolean has = bombs[r][c] != null;
                cellBombs[r][c].setVisible(has);
                if (has) cellBombs[r][c].setImage(bombImg);
            }
    }

    /**
     * Met à jour l'affichage des explosions de bombes.
     */
    private void updateExplosions() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                cellExplosions[r][c].setVisible(isExplosion[r][c]);
                if (isExplosion[r][c]) cellExplosions[r][c].setImage(explosionImg);
            }
    }

    /**
     * Vérifie si un point est dans les limites de la grille.
     * @param p Point à tester
     * @return true si le point est dans la map, false sinon
     */
    private boolean inMap(Point p) {
        return p.x >= 0 && p.x < rows && p.y >= 0 && p.y < cols;
    }

    /**
     * Affiche une popup de victoire pour le joueur donné et stoppe la partie.
     * @param player 1 ou 2 (numéro du joueur vainqueur)
     */
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

    /**
     * Indique si une case est accessible (pas de mur, bombe, ni un autre joueur).
     * @param row Ligne
     * @param col Colonne
     * @return true si la case est libre pour marcher
     */
    private boolean isWalkable(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        if (!(map[row][col].equals("pelouse"))) return false;
        if (bombs[row][col] != null) return false;
        if ((row == p1Pos.x && col == p1Pos.y) || (row == p2Pos.x && col == p2Pos.y)) return false;
        return true;
    }

    /**
     * Place une bombe sur la case indiquée pour le joueur (si autorisé).
     * Lance un timer pour l'explosion.
     * @param row Ligne
     * @param col Colonne
     * @param owner Numéro du joueur (1/2)
     * @param radius Portée de la bombe
     */
    private void placeBomb(int row, int col, int owner, int radius) {
        if (!map[row][col].equals("pelouse") || bombs[row][col] != null) return;
        if (owner == 1 && p1BombCount >= maxBombsPerPlayer) return;
        if (owner == 2 && p2BombCount >= maxBombsPerPlayer) return;
        bombs[row][col] = new Bomb(row, col, owner, radius);
        if (owner == 1) p1BombCount++;
        if (owner == 2) p2BombCount++;
        updateBombs();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> explodeBomb(row, col)));
        timeline.setCycleCount(1);
        timeline.play();
    }

    /**
     * Gère l'explosion d'une bombe :
     * - Affiche l'explosion
     * - Tue les joueurs touchés
     * - Détruit les murs destructibles touchés
     * - Gère le respawn éventuel des joueurs et flags
     * @param row Ligne de la bombe
     * @param col Colonne de la bombe
     */
    private void explodeBomb(int row, int col) {
        Bomb bomb = bombs[row][col];
        if (bomb == null) return;
        bombs[row][col] = null;
        if (bomb.owner == 1 && p1BombCount > 0) p1BombCount--;
        if (bomb.owner == 2 && p2BombCount > 0) p2BombCount--;
        
        boolean[][] exploded = new boolean[rows][cols];
        exploded[row][col] = true;

        for (int[] dir : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            for (int r=1; r<=bomb.radius; r++) {
                int nr = row + dir[0]*r, nc = col + dir[1]*r;
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) break;
                if (map[nr][nc].equals("wall")) break;
                exploded[nr][nc] = true;
                if (map[nr][nc].equals("destructible")) break;
            }
        }

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (exploded[r][c]) isExplosion[r][c] = true;
        updateBombs();
        updateExplosions();
        updatePlayersDisplay();
        
        boolean p1Killed = false, p2Killed = false;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) if (exploded[r][c]) {
                if (p1Pos.x == r && p1Pos.y == c) p1Killed = true;
                if (p2Pos.x == r && p2Pos.y == c) p2Killed = true;
                if (map[r][c].equals("destructible")) map[r][c] = "pelouse";
            }
        final boolean p1KilledFinal = p1Killed;
        final boolean p2KilledFinal = p2Killed;
        
        drawBoard();
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    if (exploded[r][c]) isExplosion[r][c] = false;
            updateExplosions();
            updatePlayersDisplay();
            
            if (!gameEnded && (p1KilledFinal || p2KilledFinal)) {

                if (p1KilledFinal) {
                    p1Pos.setLocation(p1Base);
                    p1HasFlag = false;

                    if (flag2Pos.x == -1 && flag2Pos.y == -1) {
                        flag2Pos.setLocation(p2Base);
                        p2HasFlag = false;
                    }
                }

                if (p2KilledFinal) {
                    p2Pos.setLocation(p2Base);
                    p2HasFlag = false;
                    if (flag1Pos.x == -1 && flag1Pos.y == -1) {
                        flag1Pos.setLocation(p1Base);
                        p1HasFlag = false;
                    }
                }
                updateFlags();
                updatePlayersDisplay();
            }
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    /**
     * Gère les entrées clavier pour les déplacements, la pose de bombe, la prise et la capture du flag.
     * @param event Evénement clavier reçu
     */
    private void handleKeyPressed(KeyEvent event) {
        if (gameEnded) return;
        KeyCode code = event.getCode();

        int nr1 = p1Pos.x, nc1 = p1Pos.y;
        boolean moved1 = false, bomb1 = false;
        Direction newP1Dir = p1Direction;
        switch (code) {
            case Z: nr1--; moved1 = true; newP1Dir = Direction.DOS; break;
            case S: nr1++; moved1 = true; newP1Dir = Direction.FACE; break;
            case Q: nc1--; moved1 = true; newP1Dir = Direction.GAUCHE; break;
            case D: nc1++; moved1 = true; newP1Dir = Direction.DROITE; break;
            case E: bomb1 = true; break;
        }
        if (moved1 && isWalkable(nr1, nc1)) {
            p1Pos.setLocation(nr1, nc1);
            p1Direction = newP1Dir; // Met à jour la direction
            if (!p1HasFlag && p1Pos.equals(flag2Pos)) {
                p1HasFlag = true;
                flag2Pos.setLocation(-1, -1);
            }
            if (p1HasFlag && p1Pos.equals(p1Base)) {
                scoreP1++;
                if (scoreP1Label != null) scoreP1Label.setText(String.valueOf(scoreP1));
                showWinner(1);
                return;
            }
            updateFlags();
            updatePlayersDisplay();
        }
        if (bomb1 && p1BombCount < maxBombsPerPlayer && bombs[p1Pos.x][p1Pos.y] == null && map[p1Pos.x][p1Pos.y].equals("pelouse")) {
            placeBomb(p1Pos.x, p1Pos.y, 1, bombRadius);
        }

        int nr2 = p2Pos.x, nc2 = p2Pos.y;
        boolean moved2 = false, bomb2 = false;
        Direction newP2Dir = p2Direction;
        switch (code) {
            case I: nr2--; moved2 = true; newP2Dir = Direction.DOS; break;
            case K: nr2++; moved2 = true; newP2Dir = Direction.FACE; break;
            case J: nc2--; moved2 = true; newP2Dir = Direction.GAUCHE; break;
            case L: nc2++; moved2 = true; newP2Dir = Direction.DROITE; break;
            case U: bomb2 = true; break;
        }
        if (moved2 && isWalkable(nr2, nc2)) {
            p2Pos.setLocation(nr2, nc2);
            p2Direction = newP2Dir; // Met à jour la direction
            if (!p2HasFlag && p2Pos.equals(flag1Pos)) {
                p2HasFlag = true;
                flag1Pos.setLocation(-1, -1);
            }
            if (p2HasFlag && p2Pos.equals(p2Base)) {
                scoreP2++;
                if (scoreP2Label != null) scoreP2Label.setText(String.valueOf(scoreP2));
                showWinner(2);
                return;
            }
            updateFlags();
            updatePlayersDisplay();
        }
        if (bomb2 && p2BombCount < maxBombsPerPlayer && bombs[p2Pos.x][p2Pos.y] == null && map[p2Pos.x][p2Pos.y].equals("pelouse")) {
            placeBomb(p2Pos.x, p2Pos.y, 2, bombRadius);
        }
    }

    /**
     * Renvoie l'image du joueur selon sa couleur et direction.
     * @param colorKey Couleur
     * @param dir Direction (toujours Face pour ce mode)
     * @return Image du joueur
     */
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

    /**
     * Charge une image à partir du chemin fourni.
     * Si l'image est absente, retourne une image vide.
     * @param path Chemin dans resources
     * @return Image chargée ou image vide
     */
    private Image safeLoad(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Image introuvable : " + path);
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9W5x3QAAAABJRU5ErkJggg==");
        }
        return new Image(url.toString());
    }

    /**
     * Charge une image d'asset du dossier de thème ou du fallback asset_base.
     * @param folder Dossier du thème
     * @param file Nom du fichier image
     * @return Image chargée, ou null si absente
     */
    private Image loadAsset(String folder, String file) {
        String path = "/images/" + folder + "/" + file;
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            url = getClass().getResource("/images/asset_base/" + file);
        }
        if (url == null) {
            System.err.println("Image absente: " + path + " et asset_base/" + file);
            return null;
        }
        return new Image(url.toString());
    }

    /**
     * Incrémente le timer et met à jour l'affichage du chrono.
     */
    private void updateTimer() {
        elapsedSeconds++;
        int min = elapsedSeconds / 60;
        int sec = elapsedSeconds % 60;
        if (timerLabel != null)
            timerLabel.setText(String.format("%02d:%02d", min, sec));
    }

    /**
     * Action retour au menu :
     * - Arrête le timer
     * - Coupe la musique
     * - Affiche la scène du menu principal
     */
    @FXML
    private void onBackMenu() {
        if (timerTimeline != null) timerTimeline.stop();
        BomberManApp.showMenu();
        MusicManager.stopMusic();
        BomberManApp.showMenu();
    }

    /**
     * Prépare le redimensionnement dynamique de la grille selon la fenêtre.
     */
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

    /**
     * Redimensionne la grille pour qu'elle tienne dans l'espace central, avec cases carrées.
     */
    private void resizeGridPane() {
        double paneW = gameCenterPane.getWidth();
        double paneH = gameCenterPane.getHeight();
        double cellSize = Math.min(paneW / cols, paneH / rows);
        gridPane.setPrefWidth(cellSize * cols);
        gridPane.setPrefHeight(cellSize * rows);
        gridPane.setMaxWidth(cellSize * cols);
        gridPane.setMaxHeight(cellSize * rows);
    }

    /**
     * Action bouton personnalisation : ouvre l'écran de personnalisation.
     */
    @FXML
    private void onBtnPersoClick() {
        openPersonnalisationPage();
    }

    /**
     * Ouvre la fenêtre de personnalisation : passe le contexte et callbacks au contrôleur de personnalisation.
     */
    private void openPersonnalisationPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Personnalisation.fxml"));
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


    /**
     * Action bouton pause/reprise musique.
     * Utilise MusicManager.
     */
    @FXML
    private void onPauseMusic() {
        if (MusicManager.isPaused()) {
            MusicManager.resume();
        } else {
            MusicManager.pause();
        }
    }

    /**
     * Action bouton musique suivante.
     * Utilise MusicManager.
     */
    @FXML
    private void onNextMusic() {
        MusicManager.playNextGameMusic();
    }

    /**
     * Action bouton restart musique.
     * Utilise MusicManager.
     */
    @FXML
    private void onRestartMusic() {
        MusicManager.restart();
    }
    

}