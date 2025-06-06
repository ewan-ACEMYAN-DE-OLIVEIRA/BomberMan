package BomberMan;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MenuController {
    @FXML private Button playButton;
    @FXML private Button battleButton;
    @FXML private Button customButton;
    @FXML private Button quitButton;

    @FXML
    public void initialize() {
        // Bouton pour lancer le jeu normal
        playButton.setOnAction(event -> {
            try {
                Stage menuStage = (Stage) playButton.getScene().getWindow();
                BomberManApp app = new BomberManApp();
                app.launchGame(menuStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Bouton pour accéder à la personnalisation
        customButton.setOnAction(event -> {
            try {
                Stage stage = (Stage) customButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/BomberMan/PersonnalisationView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 600, 600);
                stage.setScene(scene);
                stage.setTitle("Personnalisation");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Bouton de quitter
        quitButton.setOnAction(event -> {
            Stage stage = (Stage) quitButton.getScene().getWindow();
            stage.close();
        });

        // (Optionnel) À compléter si le "battleButton" a une fonction spécifique
        battleButton.setOnAction(event -> {
            // À implémenter si nécessaire
            System.out.println("Battle Mode - non encore implémenté");
        });
    }
}
