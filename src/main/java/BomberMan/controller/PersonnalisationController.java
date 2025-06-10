package BomberMan.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class PersonnalisationController {

    // Les couleurs disponibles
    private static final String[] COLORS = {"Blanc", "Rouge", "Bleu", "Noir", "Jacob"};


    // Indices sélectionnés pour chaque joueur
    private int indexJ1 = 0;
    private int indexJ2 = 1;

    // Composants pour Joueur 1
    private Label couleurJ1Label;
    private ImageView imageJ1;

    // Composants pour Joueur 2
    private Label couleurJ2Label;
    private ImageView imageJ2;

    // Pour ouvrir/revenir à la scène du jeu
    private Stage stage;
    private Scene previousScene;

    public void setStageAndPreviousScene(Stage stage, Scene previousScene) {
        this.stage = stage;
        this.previousScene = previousScene;
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

        leftJ1.setOnAction(e -> {
            indexJ1 = (indexJ1 - 1 + COLORS.length) % COLORS.length;
            updateJ1();
        });
        rightJ1.setOnAction(e -> {
            indexJ1 = (indexJ1 + 1) % COLORS.length;
            updateJ1();
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
            updateJ2();
        });
        rightJ2.setOnAction(e -> {
            indexJ2 = (indexJ2 + 1) % COLORS.length;
            updateJ2();
        });

        ligneJ2.getChildren().addAll(joueur2Label, leftJ2, couleurJ2Label, rightJ2, imageJ2);

        // Ajout des lignes au root
        root.getChildren().addAll(ligneJ1, ligneJ2);

        return root;
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
        if (stage != null && previousScene != null) {
            stage.setScene(previousScene);
        }
    }
}