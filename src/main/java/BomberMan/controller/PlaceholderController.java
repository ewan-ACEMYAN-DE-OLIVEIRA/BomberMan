package BomberMan.controller;

import BomberMan.application.BomberManApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Contrôleur pour la scène de placeholder (écran temporaire ou message personnalisé).
 * Permet d'afficher un message et de retourner au menu principal.
 */
public class PlaceholderController {
    /** Label pour afficher le message personnalisé */
    @FXML
    private Label messageLabel;

    /**
     * Définit le message à afficher dans le label de la scène placeholder.
     * @param msg Le message à afficher à l'utilisateur
     */
    public void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    /**
     * Action déclenchée lors du clic sur le bouton retour menu.
     * Affiche la scène du menu principal.
     */
    @FXML
    private void onBackMenu() {
        BomberManApp.showMenu();
    }
}