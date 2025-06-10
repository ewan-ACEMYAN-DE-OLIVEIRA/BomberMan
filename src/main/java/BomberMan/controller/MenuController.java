package BomberMan.controller;

import BomberMan.application.BomberManApp;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;

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
        List<String> choices = Arrays.asList("Facile", "Normal", "Difficile");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Facile", choices);
        dialog.setTitle("Choix de la difficulté");
        dialog.setHeaderText("Sélectionnez la difficulté de l'IA");
        dialog.setContentText("Difficulté :");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String difficulty = result.get();
            BomberManApp.showGame(false, difficulty);
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
}