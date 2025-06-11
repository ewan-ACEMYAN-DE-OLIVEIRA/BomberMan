package BomberMan.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class PersonnalisationController {

    private static final String[] THEMES = {"Base", "Jungle", "Désert", "Backrooms"};
    private static final String[] THEME_FOLDERS = {"asset_base", "asset_jungle", "asset_desert", "asset_backrooms"};
    // Les couleurs disponibles
    private static final String[] COLORS = {"Blanc", "Rouge", "Bleu", "Noir", "Jacob"};


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

    public Pane getView() {
        VBox root = new VBox(32);
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #222;");

        // Titre
        Label titre = new Label("Personnalisation");
        titre.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        root.getChildren().add(titre);

        // Ligne Joueur 1
        HBox ligneJ1 = new HBox(16);
        ligneJ1.setAlignment(Pos.CENTER);

        Label joueur1Label = new Label("Joueur 1");
        joueur1Label.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button leftJ1 = new Button("<");
        leftJ1.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");
        Button rightJ1 = new Button(">");
        rightJ1.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");

        couleurJ1Label = new Label(COLORS[indexJ1]);
        couleurJ1Label.setStyle("-fx-font-size: 22px; -fx-text-fill: #fff; -fx-font-weight: bold;");

        imageJ1 = new ImageView(getImagePath(COLORS[indexJ1]));
        imageJ1.setFitWidth(52);
        imageJ1.setFitHeight(52);
        imageJ1.setPreserveRatio(true);
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

        ligneJ1.getChildren().addAll(joueur1Label, leftJ1, couleurJ1Label, rightJ1, imageJ1);

        // Ligne Joueur 2
        HBox ligneJ2 = new HBox(16);
        ligneJ2.setAlignment(Pos.CENTER);

        Label joueur2Label = new Label("Joueur 2");
        joueur2Label.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button leftJ2 = new Button("<");
        leftJ2.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");
        Button rightJ2 = new Button(">");
        rightJ2.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px;");

        couleurJ2Label = new Label(COLORS[indexJ2]);
        couleurJ2Label.setStyle("-fx-font-size: 22px; -fx-text-fill: #fff; -fx-font-weight: bold;");

        imageJ2 = new ImageView(getImagePath(COLORS[indexJ2]));
        imageJ2.setFitWidth(52);
        imageJ2.setFitHeight(52);
        imageJ2.setPreserveRatio(true);

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

    private void updateJ1() {
        couleurJ1Label.setText(COLORS[indexJ1]);
        imageJ1.setImage(new Image(getImagePath(COLORS[indexJ1])));
    }

    private void updateJ2() {
        couleurJ2Label.setText(COLORS[indexJ2]);
        imageJ2.setImage(new Image(getImagePath(COLORS[indexJ2])));
    }

    private String getImagePath(String colorKey) {
        return getClass().getResource("/personnages/" + colorKey + "/face.png").toExternalForm();
    }

    // (Optionnel) Ajoute une méthode pour valider ou revenir à la scène de jeu
    public void revenirAuJeu() {
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