package BomberMan.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import BomberMan.controller.GameController;
import BomberMan.controller.PlaceholderController;

import java.io.IOException;

public class BomberManApp extends Application {
    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showMenu();
        primaryStage.setTitle("SuperBomberman");
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void showMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(BomberManApp.class.getResource("/FXML/Menu.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(BomberManApp.class.getResource("/css/bomberman.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showGame(boolean is1v1) {
        showGame(is1v1, null);
    }

    public static void showGame(boolean is1v1, String difficulty) {
        try {
            FXMLLoader loader = new FXMLLoader(BomberManApp.class.getResource("/FXML/game.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(BomberManApp.class.getResource("/css/bomberman.css").toExternalForm());
            GameController controller = loader.getController();
            controller.initGame(is1v1, difficulty); // Passe la difficulté
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showPlaceholder(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(BomberManApp.class.getResource("/FXML/placeholder.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(BomberManApp.class.getResource("/css/bomberman.css").toExternalForm());
            PlaceholderController controller = loader.getController();
            controller.setMessage(message);
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void showCTF() {
        try {
            FXMLLoader loader = new FXMLLoader(BomberManApp.class.getResource("/FXML/CaptureTheFlag.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) getPrimaryStage(); // ou le moyen que tu utilises pour avoir la fenêtre principale
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}