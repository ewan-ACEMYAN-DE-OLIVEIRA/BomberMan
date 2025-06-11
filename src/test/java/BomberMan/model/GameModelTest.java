package BomberMan.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {
    @Test
    void testPlaceBombForPlayer1() {
        GameModel model = new GameModel();

        // 1. Le joueur 1 pose une bombe sur sa case de départ
        assertTrue(model.placeBombForPlayer(1), "La première pose de bombe doit fonctionner");

        // 2. Il ne peut pas en poser une deuxième sans bouger ou explosion
        assertFalse(model.placeBombForPlayer(1), "Impossible de poser une 2e bombe au même endroit sans bouger ou explosion");

        // 3. On simule l’explosion de la bombe
        model.bombExploded(1);

        // 4. Le joueur bouge
        assertTrue(model.movePlayer1(1, 0), "Le joueur doit pouvoir bouger vers le bas");

        // 5. Il peut poser une nouvelle bombe sur la nouvelle case
        assertTrue(model.placeBombForPlayer(1), "Le joueur doit pouvoir poser une bombe sur une nouvelle case après avoir bougé");
    }
}