package BomberMan.controller;

import BomberMan.application.BomberManApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Contrôleur du menu principal du jeu BomberMan.
 * Gère le lancement des différents modes de jeu, la personnalisation, et la gestion de la musique du menu.
 */
public class MenuController {
    /**
     * Lance le mode 1v1 (deux joueurs humains).
     */
    @FXML
    private void onPlay1v1() {
        BomberManApp.showGame(true);
    }

    /**
     * Ouvre une boîte de dialogue pour choisir la difficulté de l'IA,
     * puis lance la partie contre l'IA avec la difficulté choisie.
     */
    @FXML
    private void onPlayIA() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/DifficultyDialog.fxml"));
            Parent root = loader.load();


            Stage dialogStage = new Stage();
            dialogStage.initOwner(BomberManApp.getPrimaryStage());
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/dialog-theme.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setTitle("Choix de la difficulté");


            BomberMan.controller.DifficultyDialogController controller = loader.getController();
            controller.setContext(dialogStage, difficulty -> {
                BomberManApp.showGame(false, difficulty);
            });

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Affiche un écran placeholder pour la personnalisation (fonctionnalité à venir).
     */
    @FXML
    private void onCustom() {
        BomberManApp.showPlaceholder("Personnalisation à venir...");
    }

    /**
     * Quitte l'application proprement.
     */
    @FXML
    private void onQuit() {
        System.exit(0);
    }

    /**
     * Joue la musique du menu dès l'arrivée sur le menu principal.
     */
    @FXML
    public void initialize() {
        MusicManager.playMenuMusic();
    }

    /**
     * Lance le mode Capture The Flag.
     */
    @FXML
    private void onPlayCTF() {
        BomberManApp.showCTF();
    }
}