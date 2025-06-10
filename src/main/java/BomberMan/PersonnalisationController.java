package BomberMan;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class PersonnalisationController {
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        backButton.setOnAction(event -> {
            try {
                Stage stage = (Stage) backButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MenuView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 600, 600);
                stage.setScene(scene);
                stage.setTitle("Super Bomberman");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
