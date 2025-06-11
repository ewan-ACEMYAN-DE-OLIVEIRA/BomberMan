package BomberMan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BombTest {
    @Test
    void testBombTimer() {
        Player owner = new Player("P1", 1, 1, 3);
        Bomb bomb = new Bomb(2, 2, owner);
        assertEquals(60, bomb.getTimer());
        bomb.tick();
        assertEquals(59, bomb.getTimer());
    }

    @Test
    void testBombRange() {
        Player owner = new Player("P1", 1, 1, 3);
        owner.setBombRange(4);
        Bomb bomb = new Bomb(2, 2, owner);
        assertEquals(4, bomb.getRange());
    }
}
