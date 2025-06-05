package BomberMan;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuController {
    @FXML private Button playButton;
    @FXML private Button quitButton;

    @FXML
    public void initialize() {
        playButton.setOnAction(event -> {
            try {
                // On récupère la fenêtre actuelle du menu
                Stage menuStage = (Stage) playButton.getScene().getWindow();
                // On ferme le menu et on lance la fenêtre de jeu dans la même fenêtre
                BomberManApp app = new BomberManApp();
                app.launchGame(menuStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        quitButton.setOnAction(event -> ((Stage) quitButton.getScene().getWindow()).close());
    }
}
