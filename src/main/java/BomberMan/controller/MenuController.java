package BomberMan.controller;

import BomberMan.application.BomberManApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MenuController {
    @FXML
    private void onPlay1v1() {
        BomberManApp.showGame(true);
    }

    @FXML
    private void onPlayIA() {
        try {
            // Charge la fenêtre de sélection de difficulté
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/DifficultyDialog.fxml"));
            Parent root = loader.load();

            // Crée la scène et la fenêtre modale
            Stage dialogStage = new Stage();
            dialogStage.initOwner(BomberManApp.getPrimaryStage());
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/dialog-theme.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setTitle("Choix de la difficulté");

            // Passe le callback pour lancer le jeu une fois la difficulté choisie
            BomberMan.controller.DifficultyDialogController controller = loader.getController();
            controller.setContext(dialogStage, difficulty -> {
                BomberManApp.showGame(false, difficulty);
            });

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCustom() {
        BomberManApp.showPlaceholder("Personnalisation à venir...");
    }

    @FXML
    private void onQuit() {
        System.exit(0);
    }
    
    @FXML
    public void initialize() {MusicManager.playMenuMusic();
    }
    
    @FXML
    private void onPlayCTF() {
        BomberManApp.showCTF(); // ou la méthode qui lance le mode Capture The Flag
    }
}