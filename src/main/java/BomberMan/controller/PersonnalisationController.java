package BomberMan.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Contrôleur de la page de personnalisation des joueurs et du thème.
 * Permet de sélectionner la couleur des deux joueurs ainsi que le thème visuel de la carte.
 * Applique la personnalisation via un callback et retourne à la scène précédente.
 */
public class PersonnalisationController {

    /** Noms des thèmes affichés à l'utilisateur. */
    private static final String[] THEMES = {"Base", "Jungle", "Désert", "Backrooms"};
    /** Noms des dossiers de ressources pour chaque thème. */
    private static final String[] THEME_FOLDERS = {"asset_base", "asset_jungle", "asset_desert", "asset_backrooms"};
    /** Couleurs disponibles pour les joueurs. */
    private static final String[] COLORS = {"Blanc", "Rouge", "Bleu", "Noir", "Jacob"};

    /** Indice sélectionné pour le joueur 1. */
    private int indexJ1 = 0;
    /** Indice sélectionné pour le joueur 2. */
    private int indexJ2 = 1;
    /** Indice sélectionné pour le thème. */
    private int themeIndex = 0;

    @FXML private Label couleurJ1Label;
    @FXML private ImageView imageJ1;
    @FXML private Button leftJ1;
    @FXML private Button rightJ1;

    @FXML private Label couleurJ2Label;
    @FXML private ImageView imageJ2;
    @FXML private Button leftJ2;
    @FXML private Button rightJ2;

    @FXML private Button applyBtn;
    @FXML private Button leftTheme;
    @FXML private Button rightTheme;
    @FXML private Label themeNameLabel;
    @FXML private ImageView themeImg;

    /** Stage courant pour la personnalisation. */
    private Stage stage;
    /** Scène précédente à restaurer après l'application de la personnalisation. */
    private Scene previousScene;
    /** Callback à appeler lors de l'application de la personnalisation. */
    private ApplyCallback callback;

    /**
     * Interface de callback pour l'application de la personnalisation.
     */
    public interface ApplyCallback {
        /**
         * Appelé quand l'utilisateur applique la personnalisation.
         * @param indexJ1 Indice couleur joueur 1
         * @param indexJ2 Indice couleur joueur 2
         * @param themeIndex Indice du thème
         */
        void onApply(int indexJ1, int indexJ2, int themeIndex);
    }

    /**
     * Configure le contexte de personnalisation.
     * @param stage          Stage courant
     * @param previousScene  Scène précédente à restaurer
     * @param callback       Callback à appeler lors de l'application
     * @param currentJ1      Couleur actuelle joueur 1
     * @param currentJ2      Couleur actuelle joueur 2
     * @param currentTheme   Thème actuel
     */
    public void setContext(Stage stage, Scene previousScene, ApplyCallback callback,
                           int currentJ1, int currentJ2, int currentTheme) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.callback = callback;
        this.indexJ1 = currentJ1;
        this.indexJ2 = currentJ2;
        this.themeIndex = currentTheme;
    }

    /**
     * Initialise les boutons, labels et images de personnalisation.
     * Met à jour l'affichage à chaque changement de sélection par l'utilisateur.
     */
    @FXML
    public void initialize() {
        updateJ1();
        updateJ2();
        updateTheme();

        leftJ1.setOnAction(e -> {
            indexJ1 = (indexJ1 - 1 + COLORS.length) % COLORS.length;
            updateJ1();
        });
        rightJ1.setOnAction(e -> {
            indexJ1 = (indexJ1 + 1) % COLORS.length;
            updateJ1();
        });

        leftJ2.setOnAction(e -> {
            indexJ2 = (indexJ2 - 1 + COLORS.length) % COLORS.length;
            updateJ2();
        });
        rightJ2.setOnAction(e -> {
            indexJ2 = (indexJ2 + 1) % COLORS.length;
            updateJ2();
        });

        leftTheme.setOnAction(e -> {
            themeIndex = (themeIndex - 1 + THEMES.length) % THEMES.length;
            updateTheme();
        });
        rightTheme.setOnAction(e -> {
            themeIndex = (themeIndex + 1) % THEMES.length;
            updateTheme();
        });
    }

    /**
     * Met à jour l'affichage du joueur 1 (label et image) selon la sélection.
     */
    private void updateJ1() {
        couleurJ1Label.setText(COLORS[indexJ1]);
        imageJ1.setImage(getPlayerFaceImage(COLORS[indexJ1]));
    }

    /**
     * Met à jour l'affichage du joueur 2 (label et image) selon la sélection.
     */
    private void updateJ2() {
        couleurJ2Label.setText(COLORS[indexJ2]);
        imageJ2.setImage(getPlayerFaceImage(COLORS[indexJ2]));
    }

    /**
     * Met à jour l'affichage du thème (nom et image de prévisualisation).
     */
    private void updateTheme() {
        themeNameLabel.setText(THEMES[themeIndex]);
        themeImg.setImage(getThemePreviewImage(THEME_FOLDERS[themeIndex]));
    }

    /**
     * Récupère l'image de face d'un joueur selon sa couleur.
     * @param colorKey Couleur ("Blanc", "Rouge", ...)
     * @return Image correspondante ou image vide si absente
     */
    private Image getPlayerFaceImage(String colorKey) {
        String path = "/Personnages/" + colorKey + "/Face.png";
        java.net.URL url = getClass().getResource(path);
        if (url == null) url = getClass().getResource("/Personnages/Blanc/Face.png");
        if (url == null) return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9W5x3QAAAABJRU5ErkJggg==");
        return new Image(url.toString());
    }

    /**
     * Récupère l'image de prévisualisation du thème sélectionné.
     * @param themeFolder Nom du dossier de thème
     * @return Image de la map du thème, ou image vide si absente
     */
    private Image getThemePreviewImage(String themeFolder) {
        String path = "/images/" + themeFolder + "/map.png";
        java.net.URL url = getClass().getResource(path);
        if (url == null) url = getClass().getResource("/images/asset_base/map.png");
        if (url == null) return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9W5x3QAAAABJRU5ErkJggg==");
        return new Image(url.toString());
    }

    /**
     * Action du bouton "Appliquer" :
     * - Appelle le callback pour enregistrer la personnalisation
     * - Restaure la scène précédente
     */
    @FXML
    private void appliquerAction() {
        if (callback != null) {
            callback.onApply(indexJ1, indexJ2, themeIndex);
        }
        if (stage != null && previousScene != null) {
            stage.setScene(previousScene);
            stage.show();
        }
    }
}