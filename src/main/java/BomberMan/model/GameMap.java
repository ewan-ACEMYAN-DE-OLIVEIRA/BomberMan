package BomberMan.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Représente la carte du jeu BomberMan.
 * Permet de charger une carte à partir d'une ressource texte, de manipuler les tuiles,
 * et de générer des murs cassables aléatoirement tout en respectant des zones interdites.
 */
public class GameMap {
    /** Grille des tuiles de la carte. */
    private Tile[][] tiles;
    /** Largeur (nombre de colonnes) de la carte. */
    private int width;
    /** Hauteur (nombre de lignes) de la carte. */
    private int height;

    /**
     * Charge et construit la carte à partir d'un fichier ressource.
     * @param resourcePath Chemin de la ressource texte (ex: "/maps/map1.txt")
     */
    public GameMap(String resourcePath) {
        loadMap(resourcePath);
    }

    /**
     * Charge le contenu de la map depuis le fichier ressource donné,
     * et initialise la grille des tuiles.
     * Les caractères '#' donnent des murs indestructibles,
     * '.' des cases libres, '*' des murs cassables.
     * @param resourcePath Chemin de la ressource à charger
     */
    private void loadMap(String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
            height = lines.size();
            width = lines.get(0).length();
            tiles = new Tile[height][width];
            for (int r = 0; r < height; r++) {
                String l = lines.get(r);
                for (int c = 0; c < width; c++) {
                    switch (l.charAt(c)) {
                        case '#' -> tiles[r][c] = Tile.WALL;
                        case '.' -> tiles[r][c] = Tile.FLOOR;
                        case '*' -> tiles[r][c] = Tile.BREAKABLE;
                        default -> tiles[r][c] = Tile.FLOOR;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load map: " + resourcePath, e);
        }
    }

    /**
     * Place aléatoirement des murs cassables sur la carte, en évitant toutes les cases fournies dans forbiddenZones.
     * @param count Nombre de murs cassables à placer
     * @param forbiddenZones Liste de positions interdites (chaque int[] représente {ligne, colonne})
     */
    public void generateRandomBreakables(int count, List<int[]> forbiddenZones) {
        Random rand = new Random();
        List<int[]> freeTiles = new ArrayList<>();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (tiles[r][c] == Tile.FLOOR) {
                    boolean forbidden = false;
                    for (int[] pos : forbiddenZones) {
                        if (pos[0] == r && pos[1] == c) {
                            forbidden = true;
                            break;
                        }
                    }
                    if (!forbidden) freeTiles.add(new int[]{r, c});
                }
            }
        }
        Collections.shuffle(freeTiles, rand);
        for (int i = 0; i < Math.min(count, freeTiles.size()); i++) {
            int[] pos = freeTiles.get(i);
            tiles[pos[0]][pos[1]] = Tile.BREAKABLE;
        }
    }

    /**
     * Retourne la tuile en (row, col), ou un mur si hors de la carte.
     * @param row Ligne demandée
     * @param col Colonne demandée
     * @return Tuile correspondante, ou WALL si hors limites
     */
    public Tile getTile(int row, int col) {
        if (row < 0 || col < 0 || row >= height || col >= width) return Tile.WALL;
        return tiles[row][col];
    }

    /**
     * Modifie la tuile à la position donnée.
     * Ne fait rien si la position est hors carte.
     * @param row Ligne à modifier
     * @param col Colonne à modifier
     * @param tile Tuile à placer
     */
    public void setTile(int row, int col, Tile tile) {
        if (row < 0 || col < 0 || row >= height || col >= width) return;
        tiles[row][col] = tile;
    }

    /**
     * Retourne la largeur de la carte (nombre de colonnes).
     * @return Largeur (int)
     */
    public int getWidth() { return width; }

    /**
     * Retourne la hauteur de la carte (nombre de lignes).
     * @return Hauteur (int)
     */
    public int getHeight() { return height; }
}