package sk.tuke.gamestudio.game.logicalmazes;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.game.logicalmazes.core.Tile;
import sk.tuke.gamestudio.game.logicalmazes.core.TileType;

import static org.junit.jupiter.api.Assertions.*;

public class TileTest {

    @Test
    public void tileTest() {
        Tile tile = new Tile(TileType.CLEAR);
        assertEquals(TileType.CLEAR, tile.getType());

        tile.setType(TileType.TARGET);
        assertEquals(TileType.TARGET, tile.getType());
    }
}
