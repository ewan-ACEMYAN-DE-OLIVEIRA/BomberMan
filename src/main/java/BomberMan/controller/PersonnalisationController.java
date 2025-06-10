package BomberMan.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class PersonnalisationController {

    private static final String[] COLORS = {"Blanc", "Rouge", "Bleu", "Noir"};
    private static final String[] COLOR_KEYS = {"blanc", "rouge", "bleu", "noir"};
    private static final String[] THEMES = {"Base", "Jungle", "Désert", "Backrooms"};
    private static final String[] THEME_FOLDERS = {"asset_base", "asset_jungle", "asset_desert", "asset_backrooms"};

    private int indexJ1 = 0;
    private int indexJ2 = 1;
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

    private Stage stage;
    private Scene previousScene;
    private ApplyCallback callback;

    public interface ApplyCallback {
        void onApply(int indexJ1, int indexJ2, int themeIndex);
    }

    public void setContext(Stage stage, Scene previousScene, ApplyCallback callback,
                           int currentJ1, int currentJ2, int currentTheme) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.callback = callback;
        this.indexJ1 = currentJ1;
        this.indexJ2 = currentJ2;
        this.themeIndex = currentTheme;
    }

    @FXML
    public void initialize() {
        majJ1();
        majJ2();
        majTheme();

        leftJ1.setOnAction(e -> {
            indexJ1 = (indexJ1 - 1 + COLORS.length) % COLORS.length;
            majJ1();
        });
        rightJ1.setOnAction(e -> {
            indexJ1 = (indexJ1 + 1) % COLORS.length;
            majJ1();
        });

        leftJ2.setOnAction(e -> {
            indexJ2 = (indexJ2 - 1 + COLORS.length) % COLORS.length;
            majJ2();
        });
        rightJ2.setOnAction(e -> {
            indexJ2 = (indexJ2 + 1) % COLORS.length;
            majJ2();
        });

        leftTheme.setOnAction(e -> {
            themeIndex = (themeIndex - 1 + THEMES.length) % THEMES.length;
            majTheme();
        });
        rightTheme.setOnAction(e -> {
            themeIndex = (themeIndex + 1) % THEMES.length;
            majTheme();
        });
    }

    @FXML
    private void appliquerAction() {
        // Appelle le callback pour notifier GameController
        if (callback != null) {
            callback.onApply(indexJ1, indexJ2, themeIndex);
        }
        // Réaffiche la scène de jeu avec les modifs
        if (stage != null && previousScene != null) {
            stage.setScene(previousScene);
            stage.show();
        }
    }

    private void majJ1() {
        couleurJ1Label.setText(COLORS[indexJ1]);
        imageJ1.setImage(getPlayerFaceImage(COLOR_KEYS[indexJ1]));
    }

    private void majJ2() {
        couleurJ2Label.setText(COLORS[indexJ2]);
        imageJ2.setImage(getPlayerFaceImage(COLOR_KEYS[indexJ2]));
    }

    private void majTheme() {
        themeNameLabel.setText(THEMES[themeIndex]);
        themeImg.setImage(getThemePreviewImage(THEME_FOLDERS[themeIndex]));
    }

    private Image getPlayerFaceImage(String colorKey) {
        String path = "/personnages/" + colorKey + "/face.png";
        java.net.URL url = getClass().getResource(path);
        if (url == null) url = getClass().getResource("/personnages/blanc/face.png");
        if (url == null) return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9W5x3QAAAABJRU5ErkJggg==");
        return new Image(url.toString());
    }

    private Image getThemePreviewImage(String themeFolder) {
        String path = "/images/" + themeFolder + "/map.png";
        java.net.URL url = getClass().getResource(path);
        if (url == null) url = getClass().getResource("/images/asset_base/map.png");
        if (url == null) return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9W5x3QAAAABJRU5ErkJggg==");
        return new Image(url.toString());
    }
}