package sk.tuke.gamestudio.game.logicalmazes.core;

import org.junit.jupiter.api.Test;

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
