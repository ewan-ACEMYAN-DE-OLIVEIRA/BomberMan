package BomberMan.controller;

import BomberMan.application.BomberManApp;
import javafx.fxml.FXML;

public class MenuController {
    @FXML
    private void onPlay1v1() {
        BomberManApp.showGame(true);
    }

    @FXML
    private void onPlayIA() {
        BomberManApp.showPlaceholder("Mode IA à venir...");
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