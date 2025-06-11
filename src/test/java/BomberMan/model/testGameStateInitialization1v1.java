package BomberMan.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    @Test
    void testGameStateInitialization1v1() {
        GameMap map = new GameMap("/maps/test_map.txt");
        GameState state = new GameState(map, true);
        assertEquals(map, state.getMap());
        assertEquals(2, state.getPlayers().size());
        assertTrue(state.getBombs().isEmpty());
        assertTrue(state.getExplosions().isEmpty());
    }

    @Test
    void testGameStateInitializationSolo() {
        GameMap map = new GameMap("/maps/test_map.txt");
        GameState state = new GameState(map, false);
        assertEquals(1, state.getPlayers().size());
    }
}