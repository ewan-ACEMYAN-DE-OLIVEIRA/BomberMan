package BomberMan.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    @Test
    void testLoseLife() {
        Player p = new Player("P1", 1, 1, 2);
        assertTrue(p.isAlive());
        p.loseLife();
        assertEquals(1, p.getLives());
        p.loseLife();
        assertEquals(0, p.getLives());
        assertFalse(p.isAlive());
    }

    @Test
    void testIncreaseBombRange() {
        Player p = new Player("P1", 1, 1, 3);
        assertEquals(1, p.getBombRange());
        p.increaseBombRange();
        assertEquals(2, p.getBombRange());
    }

    @Test
    void testSetBombRange() {
        Player p = new Player("P1", 1, 1, 3);
        p.setBombRange(5);
        assertEquals(5, p.getBombRange());
    }
}