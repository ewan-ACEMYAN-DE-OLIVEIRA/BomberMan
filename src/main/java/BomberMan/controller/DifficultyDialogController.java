package BomberMan.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Contrôleur de la boîte de dialogue de choix de difficulté pour l'IA.
 * Permet à l'utilisateur de choisir entre Facile, Normal ou Difficile,
 * affiche une description et confirme le choix via un callback.
 */
public class DifficultyDialogController {

    @FXML private Button easyBtn;
    @FXML private Button normalBtn;
    @FXML private Button hardBtn;
    @FXML private Label descLabel;
    @FXML private Button confirmBtn;

    /** La difficulté sélectionnée ("Facile", "Normal" ou "Difficile"). */
    private String selectedDifficulty = null;
    /** Fenêtre de dialogue associée à ce contrôleur. */
    private Stage dialogStage;
    /** Callback à appeler lors de la confirmation du choix. */
    private DifficultyCallback callback;

    /**
     * Interface à implémenter pour recevoir le choix de difficulté.
     */
    public interface DifficultyCallback {
        /**
         * Méthode appelée lors de la confirmation du choix de difficulté.
         * @param difficulty La difficulté choisie ("Facile", "Normal", "Difficile")
         */
        void onDifficultySelected(String difficulty);
    }

    /**
     * Définit le contexte de la boîte de dialogue : la fenêtre et le callback à utiliser.
     * @param dialogStage La fenêtre de dialogue (Stage)
     * @param callback    Le callback à appeler quand l'utilisateur confirme
     */
    public void setContext(Stage dialogStage, DifficultyCallback callback) {
        this.dialogStage = dialogStage;
        this.callback = callback;
    }

    /**
     * Initialise les actions des boutons et l'état initial de la boîte de dialogue.
     * Chaque bouton de difficulté met à jour la sélection, la description, et active le bouton de confirmation.
     */
    @FXML
    public void initialize() {
        easyBtn.setOnAction(e -> selectDifficulty("Facile"));
        normalBtn.setOnAction(e -> selectDifficulty("Normal"));
        hardBtn.setOnAction(e -> selectDifficulty("Difficile"));

        confirmBtn.setOnAction(e -> {
            if (selectedDifficulty != null && callback != null) {
                callback.onDifficultySelected(selectedDifficulty);
                if (dialogStage != null) dialogStage.close();
            }
        });
        confirmBtn.setDisable(true);

    }

    /**
     * Sélectionne une difficulté, met à jour les styles des boutons,
     * affiche la description correspondante et active le bouton "confirmer".
     * @param diff La difficulté choisie ("Facile", "Normal", "Difficile")
     */
    private void selectDifficulty(String diff) {
        this.selectedDifficulty = diff;
        easyBtn.getStyleClass().remove("selected");
        normalBtn.getStyleClass().remove("selected");
        hardBtn.getStyleClass().remove("selected");

        switch (diff) {
            case "Facile":
                easyBtn.getStyleClass().add("selected");
                descLabel.setText("Mode facile : l'IA se déplace au hasard et pose peu de bombes.");
                break;
            case "Normal":
                normalBtn.getStyleClass().add("selected");
                descLabel.setText("Mode normal : l'IA fuit les explosions et pose ses bombes pour casser ou tuer.");
                break;
            case "Difficile":
                hardBtn.getStyleClass().add("selected");
                descLabel.setText("Mode difficile : l'IA traque, casse les murs, anticipe les dangers, et joue intelligemment.");
                break;
        }
        confirmBtn.setDisable(false);
    }
}