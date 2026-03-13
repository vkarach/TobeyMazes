package sk.tuke.gamestudio.game.logicalmazes.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class FieldTest {
    private final Field field;
    private final Player player;

    public FieldTest() {
        MapParser.Result result = new MapParser().parseMap("maps/test_map.txt");
        this.field = result.mapField();
        this.player = result.player();
    }

    @Test
    public void canStepCheck() {
        assertTrue(field.canStep(player, Direction.LEFT));
        assertFalse(field.canStep(player, Direction.RIGHT));

        assertFalse(field.canStep(player, Direction.UP));
        assertTrue(field.canStep(player, Direction.DOWN));
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT,  -1,  0, true",
            "RIGHT,  1,  0, false",
            "UP,     0, -1, false",
            "DOWN,   0,  1, true"
    })
    public void stepCheck(Direction direction, int dx, int dy, boolean shouldMove) {
        int px = player.getX();
        int py = player.getY();

        field.step(player, direction);

        if (shouldMove) {
            assertEquals(px + dx, player.getX());
            assertEquals(py + dy, player.getY());
        }
        else {
            assertEquals(px, player.getX());
            assertEquals(py, player.getY());
        }
    }

    @Test
    public void onTargetCheck() {
        assertFalse(field.onTarget(player));
        player.setPosition(2, 2);
        assertTrue(field.onTarget(player));
    }

    @Test
    public void takeTargetCheck() {
        assertFalse(field.takeTarget(player));

        int targetX = -1;
        int targetY = -1;
        for (int row = 0; row < field.getRowCount(); row++) {
            for (int col = 0; col < field.getColCount(); col++) {
                if (field.getTiles()[row][col].getType() == TileType.TARGET) {
                    targetX = col;
                    targetY = row;
                    break;
                }
            }
        }
        assertTrue(targetX != -1, "No TARGET tile found in test map");

        player.setPosition(targetX, targetY);
        assertEquals(TileType.TARGET, field.getTiles()[targetY][targetX].getType());

        assertTrue(field.takeTarget(player));
        assertEquals(TileType.CLEAR, field.getTiles()[targetY][targetX].getType());

        assertFalse(field.takeTarget(player));
    }
}