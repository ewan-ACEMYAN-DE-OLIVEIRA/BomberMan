package BomberMan;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuController {
    @FXML private Button playButton;
    @FXML private Button quitButton;
    @FXML private Button battleButton;

    @FXML
    public void initialize() {
        playButton.setOnAction(event -> launchGame(false));
        battleButton.setOnAction(event -> launchGame(true));
        quitButton.setOnAction(event -> ((Stage) quitButton.getScene().getWindow()).close());
    }

    private void launchGame(boolean vsIA) {
        Stage menuStage = (Stage) playButton.getScene().getWindow();
        try {
            BomberManApp app = new BomberManApp();
            if (vsIA) {
                app.launchGameVsIA(menuStage);
            } else {
                app.launchGame(menuStage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}