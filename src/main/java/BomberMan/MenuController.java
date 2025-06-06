package BomberMan;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MenuController {
    @FXML private Button playButton;
    @FXML private Button quitButton;
    @FXML private Button customButton;
    @FXML private Button battleButton;

    @FXML
    public void initialize() {
        playButton.setOnAction(event -> launchGame(false));
        battleButton.setOnAction(event -> launchGame(true));
        quitButton.setOnAction(event -> ((Stage) quitButton.getScene().getWindow()).close());
        customButton.setOnAction(event -> openCustomisationView());
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
    private void openCustomisationView() {
        changeScene("/com/example/BomberMan/FXML/PersonnalisationView.fxml", "Personnalisation", 600, 600, customButton);
    }
    private void changeScene(String fxmlPath, String title, int width, int height, Button sourceButton) {
        try {
            Stage stage = (Stage) sourceButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}