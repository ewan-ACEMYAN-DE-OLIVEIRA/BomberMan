package BomberMan.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExplosionTest {
    @Test
    void testExplosionTimer() {
        Explosion explosion = new Explosion(2, 3);
        assertEquals(30, explosion.getTimer());
        explosion.tick();
        assertEquals(29, explosion.getTimer());
    }

    @Test
    void testExplosionCoordinates() {
        Explosion explosion = new Explosion(5, 7);
        assertEquals(5, explosion.getRow());
        assertEquals(7, explosion.getCol());
    }
}