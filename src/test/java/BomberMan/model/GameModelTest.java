package BomberMan.model;

import BomberMan.CellType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {
    @Test
    void testPlaceBombForPlayer1() {
        GameModel model = new GameModel();

        //Le joueur 1 pose une bombe sur sa case de départ
        assertTrue(model.placeBombForPlayer(1), "La première pose de bombe doit fonctionner");
        //Il ne peut pas en poser une deuxième sans bouger ou explosion
        assertFalse(model.placeBombForPlayer(1), "Impossible de poser une 2e bombe au même endroit sans bouger ou explosion");
        //On simule l’explosion de la bombe
        model.bombExploded(1);
        //Le joueur bouge
        assertTrue(model.movePlayer1(1, 0), "Le joueur doit pouvoir bouger vers le bas");
        //Il peut poser une nouvelle bombe sur la nouvelle case
        assertTrue(model.placeBombForPlayer(1), "Le joueur doit pouvoir poser une bombe sur une nouvelle case après avoir bougé");
    }
    @Test
    void testMovePlayer1BlockedByWall() {
        GameModel model = new GameModel();
        // Place un mur devant le joueur
        model.setCellType(1, 2, CellType.WALL);
        assertFalse(model.movePlayer1(0, 1), "Le joueur ne doit pas pouvoir traverser un mur");
    }
    @Test
    void testPlayer1PicksBonus() {
        GameModel model = new GameModel();
        // Place un bonus à droite du joueur
        model.setCellType(1, 2, CellType.BONUS_RANGE);
        int oldRange = model.getBombRange1();
        assertTrue(model.movePlayer1(0, 1), "Le joueur doit pouvoir aller sur la case du bonus");
        assertEquals(oldRange + 1, model.getBombRange1(), "La portée doit augmenter après bonus");
    }
}