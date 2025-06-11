package BomberMan.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DifficultyDialogController {

    @FXML private Button easyBtn;
    @FXML private Button normalBtn;
    @FXML private Button hardBtn;
    @FXML private Label descLabel;
    @FXML private Button confirmBtn;

    private String selectedDifficulty = null; // "Facile", "Normal", "Difficile"
    private Stage dialogStage;
    private DifficultyCallback callback;

    public interface DifficultyCallback {
        void onDifficultySelected(String difficulty);
    }

    public void setContext(Stage dialogStage, DifficultyCallback callback) {
        this.dialogStage = dialogStage;
        this.callback = callback;
    }

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
    }

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