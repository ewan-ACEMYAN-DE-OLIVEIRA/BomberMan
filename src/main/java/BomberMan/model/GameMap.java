package BomberMan.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class GameMap {
    private Tile[][] tiles;
    private int width, height;

    public GameMap(String resourcePath) {
        loadMap(resourcePath);
    }

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
     * Place aléatoirement des murs cassables sur la map, en évitant toutes les cases fournies dans forbiddenZones.
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

    public Tile getTile(int row, int col) {
        if (row < 0 || col < 0 || row >= height || col >= width) return Tile.WALL;
        return tiles[row][col];
    }

    public void setTile(int row, int col, Tile tile) {
        if (row < 0 || col < 0 || row >= height || col >= width) return;
        tiles[row][col] = tile;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}