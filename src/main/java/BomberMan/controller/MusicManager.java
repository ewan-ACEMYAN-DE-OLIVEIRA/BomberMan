package BomberMan.controller; // Mets le bon package si besoin

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.List;
import java.util.Arrays;

public class MusicManager {
    private static MediaPlayer currentPlayer = null;
    private static String currentMusic = "";
    private static boolean paused = false;
    
    // ---- Liste des musiques du jeu ----
    private static final List<String> GAME_MUSICS = Arrays.asList(
            "/Musique/background1.mp3",
            "/Musique/background2.mp3",
            "/Musique/background3.mp3",
            "/Musique/background4.mp3",
            "/Musique/background5.mp3",
            "/Musique/background6.mp3",
            "/Musique/background7.mp3",
            "/Musique/background8.mp3",
            "/Musique/background9.mp3",
            "/Musique/background10.mp3",
            "/Musique/background11.mp3",
            "/Musique/background12.mp3",
            "/Musique/background13.mp3",
            "/Musique/background14.mp3"
    );
    private static int currentGameMusicIndex = 0;
    
    // ---- Musique du menu ----
    private static final String MENU_MUSIC = "/Musique/menu.mp3";
    
    // ---- Lecture de la musique du menu ----
    public static void playMenuMusic() {
        playMusic(MENU_MUSIC, true);
    }
    
    // ---- Lecture de la musique de jeu (boucle sur la liste) ----
    public static void playGameMusic() {
        currentGameMusicIndex = 0;
        playMusic(GAME_MUSICS.get(currentGameMusicIndex), true);
    }
    
    // ---- Passe à la musique de jeu suivante ----
    public static void playNextGameMusic() {
        currentGameMusicIndex = (currentGameMusicIndex + 1) % GAME_MUSICS.size();
        playMusic(GAME_MUSICS.get(currentGameMusicIndex), true);
    }
    
    // ---- Redémarre la musique en cours ----
    public static void restart() {
        if (currentPlayer != null) {
            currentPlayer.seek(javafx.util.Duration.ZERO);
            if (paused) {
                resume();
            }
        }
    }
    
    // ---- Pause ----
    public static void pause() {
        if (currentPlayer != null) {
            currentPlayer.pause();
            paused = true;
        }
    }
    
    // ---- Reprendre ----
    public static void resume() {
        if (currentPlayer != null) {
            currentPlayer.play();
            paused = false;
        }
    }
    
    // ---- Arrêter toute musique ----
    public static void stopMusic() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
        }
        paused = false;
        currentMusic = "";
    }
    
    // ---- Est-ce en pause ? ----
    public static boolean isPaused() {
        return paused;
    }
    
    // ---- Est-ce la musique demandée qui joue ? ----
    public static boolean isPlaying(String resourcePath) {
        return resourcePath.equals(currentMusic) && !paused;
    }
    
    // ---- Méthode générale pour jouer un fichier ----
    private static void playMusic(String resourcePath, boolean loop) {
        stopMusic(); // Coupe la musique précédente
        URL url = MusicManager.class.getResource(resourcePath);
        if (url == null) {
            System.err.println("Musique introuvable : " + resourcePath);
            return;
        }
        Media media = new Media(url.toExternalForm());
        currentPlayer = new MediaPlayer(media);
        if (loop) {
            currentPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }
        currentPlayer.play();
        paused = false;
        currentMusic = resourcePath;
    }
}