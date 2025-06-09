package BomberMan.controller;

import BomberMan.application.BomberManApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PlaceholderController {
    @FXML
    private Label messageLabel;

    public void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    @FXML
    private void onBackMenu() {
        BomberManApp.showMenu();
    }
}