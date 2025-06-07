package BomberMan.application;

import BomberMan.controller.GameViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BomberManApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MenuView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 600); // Taille fixe pour le menu
            primaryStage.setTitle("Super Bomberman");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchGame(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/GameView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root); // Pas de taille fixe
            scene.getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
            stage.setTitle("BomberMan - JavaFX");
            stage.setScene(scene);
            stage.setResizable(true); // Permet le redimensionnement si souhaité
            stage.show();
            stage.sizeToScene(); // Ajuste exactement à la taille du FXML
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchGameVsIA(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/GameView.fxml"));
            Parent root = loader.load();
            GameViewController controller = loader.getController();
            controller.setVsIA(true);
            controller.postInit();
            Scene scene = new Scene(root); // Pas de taille fixe
            scene.getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
            stage.setTitle("BomberMan - Mode Ordinateur");
            stage.setScene(scene);
            stage.setResizable(true); // Permet le redimensionnement si souhaité
            stage.show();
            stage.sizeToScene(); // Ajuste exactement à la taille du FXML
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
