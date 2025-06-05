package BomberMan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BomberManApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Afficher le menu au démarrage
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/BomberMan/MenuView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setTitle("Super Bomberman");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Cette méthode pourra être appelée depuis MenuController pour lancer le jeu
    public void launchGame(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/BomberMan/GameView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/BomberMan/game.css").toExternalForm());
        stage.setTitle("BomberMan - JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
