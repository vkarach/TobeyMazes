package sk.tuke.gamestudio.game.logicalmazes.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapParserTest {
    private final MapParser.Result mapParserResult;

    public MapParserTest() {
        this.mapParserResult = new MapParser().parseMap("maps/test_map.txt");
    }
    @Test
    public void WidthHeighChek() {
        Field field = mapParserResult.mapField;
        assertEquals(3, field.getRowCount());
        assertEquals(3, field.getRowCount());
    }

    @Test
    public void PlayerSpawnCheck() {
        Player player = mapParserResult.player;
        assertEquals(2, player.getX());
        assertEquals(0, player.getY());
    }

    @Test
    public void TargetCountCheck() {
        int targetCount = mapParserResult.targetCount;
        assertEquals(2, targetCount);
    }

}
