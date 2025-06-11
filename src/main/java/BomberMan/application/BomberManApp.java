package BomberMan.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import BomberMan.controller.GameController;
import BomberMan.controller.PlaceholderController;

import java.io.IOException;

/**
 * Classe principale de l'application BomberMan.
 * Gère le démarrage de l'application et la navigation entre les différentes scènes (menu, jeu, placeholder, CTF).
 */
public class BomberManApp extends Application {
    /** Stage principal partagé par toute l'application (fenêtre unique) */
    private static Stage primaryStage;

    /**
     * Retourne l'instance du stage principal de l'application.
     * @return le Stage principal utilisé pour afficher les différentes scènes.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Point d'entrée JavaFX. Initialise la fenêtre principale et affiche le menu.
     * @param stage Le Stage primaire fourni par la plateforme JavaFX.
     * @throws Exception Si une erreur survient lors du chargement du menu.
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showMenu();
        primaryStage.setTitle("SuperBomberman");
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    /**
     * Affiche la scène du menu principal.
     * Charge le FXML du menu principal et applique le style CSS global.
     * En cas d'erreur, affiche la trace de l'exception.
     */
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

    /**
     * Affiche la scène principale du jeu en mode 1v1 ou joueur contre IA (difficulté par défaut).
     * @param is1v1 true pour 1v1 humain, false pour joueur vs IA.
     */
    public static void showGame(boolean is1v1) {
        showGame(is1v1, null);
    }

    /**
     * Affiche la scène principale du jeu en mode 1v1 ou joueur contre IA avec difficulté donnée.
     * Charge le FXML du jeu, applique le CSS, puis initialise le contrôleur de jeu avec le mode/difficulté.
     * @param is1v1 true pour 1v1 humain, false pour joueur vs IA.
     * @param difficulty Difficulté de l'IA ("Facile", "Normal", "Difficile"), ou null pour 1v1.
     */
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

    /**
     * Affiche une page temporaire (placeholder) personnalisée avec un message.
     * Utile pour afficher un écran "En construction" ou un message d'erreur.
     * @param message Message à afficher sur l'écran placeholder.
     */
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

    /**
     * Affiche la scène du mode Capture The Flag (CTF).
     * Charge le FXML dédié au mode CTF et l'affiche dans la fenêtre principale.
     */
    public static void showCTF() {
        try {
            FXMLLoader loader = new FXMLLoader(BomberManApp.class.getResource("/FXML/CaptureTheFlag.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) getPrimaryStage(); // récupère le stage principal
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Point d'entrée principal du programme Java.
     * Lance l'application JavaFX.
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        launch(args);
    }
}