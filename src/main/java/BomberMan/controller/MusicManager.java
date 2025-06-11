package BomberMan.controller;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.List;
import java.util.Arrays;

/**
 * MusicManager est une classe utilitaire statique qui gère la lecture des musiques
 * de fond pour le jeu BomberMan (musique du menu, musique en jeu, etc.).
 * Elle propose des méthodes pour lancer, arrêter, mettre en pause, reprendre et changer de musique.
 */
public class MusicManager {
    /** Lecteur audio courant. */
    private static MediaPlayer currentPlayer = null;
    /** Chemin de la musique actuellement en lecture. */
    private static String currentMusic = "";
    /** Indique si la musique est en pause. */
    private static boolean paused = false;

    /** Liste des musiques de fond pour le jeu. */
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
    /** Index de la musique de jeu actuellement jouée. */
    private static int currentGameMusicIndex = 0;

    /** Chemin de la musique du menu principal. */
    private static final String MENU_MUSIC = "/Musique/menu.mp3";

    /**
     * Joue la musique du menu principal en boucle.
     */
    public static void playMenuMusic() {
        playMusic(MENU_MUSIC, true);
    }

    /**
     * Joue la première musique de la liste des musiques de jeu en boucle.
     */
    public static void playGameMusic() {
        currentGameMusicIndex = 0;
        playMusic(GAME_MUSICS.get(currentGameMusicIndex), true);
    }

    /**
     * Passe à la musique suivante de la liste de jeu (en boucle).
     */
    public static void playNextGameMusic() {
        currentGameMusicIndex = (currentGameMusicIndex + 1) % GAME_MUSICS.size();
        playMusic(GAME_MUSICS.get(currentGameMusicIndex), true);
    }

    /**
     * Redémarre la musique courante depuis le début.
     * Si la musique était en pause, elle est relancée.
     */
    public static void restart() {
        if (currentPlayer != null) {
            currentPlayer.seek(javafx.util.Duration.ZERO);
            if (paused) {
                resume();
            }
        }
    }

    /**
     * Met la musique en pause.
     */
    public static void pause() {
        if (currentPlayer != null) {
            currentPlayer.pause();
            paused = true;
        }
    }

    /**
     * Reprend la musique si elle était en pause.
     */
    public static void resume() {
        if (currentPlayer != null) {
            currentPlayer.play();
            paused = false;
        }
    }

    /**
     * Arrête et coupe toute musique en cours.
     */
    public static void stopMusic() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
        }
        paused = false;
        currentMusic = "";
    }

    /**
     * Indique si la musique est actuellement en pause.
     * @return true si en pause, false sinon
     */
    public static boolean isPaused() {
        return paused;
    }

    /**
     * Indique si la musique donnée (resourcePath) est celle actuellement en lecture (et non en pause).
     * @param resourcePath Chemin de la ressource musicale
     * @return true si la musique demandée est en lecture, false sinon
     */
    public static boolean isPlaying(String resourcePath) {
        return resourcePath.equals(currentMusic) && !paused;
    }

    /**
     * Méthode interne pour lancer la lecture d'un fichier musical.
     * Arrête la musique précédente, charge la nouvelle et la joue éventuellement en boucle.
     * @param resourcePath Chemin de la ressource musicale à jouer
     * @param loop true pour boucler la musique, false sinon
     */
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