package BomberMan.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameMapTest {
    @Test
    void testLoadMap() {
        GameMap map = new GameMap("/maps/test_map.txt"); // Crée un fichier test_map.txt dans resources
        assertEquals(Tile.WALL, map.getTile(0, 0));
        assertEquals(Tile.FLOOR, map.getTile(1, 1));
    }

    @Test
    void testSetTile() {
        GameMap map = new GameMap("/maps/test_map.txt");
        map.setTile(1, 1, Tile.BREAKABLE);
        assertEquals(Tile.BREAKABLE, map.getTile(1, 1));
    }
}