package sk.tuke.gamestudio.game.logicalmazes.console;

import sk.tuke.gamestudio.game.logicalmazes.core.Field;
import sk.tuke.gamestudio.game.logicalmazes.core.Player;
import sk.tuke.gamestudio.game.logicalmazes.core.Tile;
import sk.tuke.gamestudio.game.logicalmazes.core.TileType;

public class ConsoleUI {
    private final Console console;

    public ConsoleUI(Console console) {
        this.console = console;
    }

    private void renderHorizontalWallLine(boolean[][] hWalls, int rowIndex, int colCount) {
        for (int col = 0; col < colCount; col++) {
            console.print('+');
            if (hWalls[rowIndex][col]) {
                console.print("--");
            } else {
                console.print("  ");
            }
        }
        console.print('+');
        console.print('\n');
    }

    private String getTimerString(long startTime) {
        long now = System.nanoTime();
        long durationMs = (now - startTime) / 1_000_000;

        long seconds = durationMs / 1000;
        long twoDigits = (durationMs % 1000) / 10; // .67

        String clock = "\uD83D\uDD50";

        return String.format("%s %d:%02d", clock, seconds, twoDigits);
    }

    public void renderHud(long startTime, int targetCount, int x, int y) {
        String timeString     = String.format("| %-10s|", getTimerString(startTime));
        String targetCountStr = String.format("| %-10s|", "target: " + String.valueOf(targetCount));

        String vBound = "+" + "-".repeat(11) + "+";

        console.print(vBound, x, y); y++;
        console.print(timeString, x, y); y++;
        console.print(targetCountStr, x, y); y++;
        console.print(vBound, x, y); y++;
    }

    public void renderGameField(Field mapField, Player player, boolean clear) {
        if (clear) {
            console.moveCursorToStart();
        }
        renderGameField(mapField, player);
    }

    public void renderGameField(Field mapField, Player player) {
        char playerCh = '♞';

        int px = player.getX();
        int py = player.getY();

        int rowCount = mapField.getRowCount();
        int colCount = mapField.getColCount();

        Tile[][] tiles = mapField.getTiles();
        boolean[][] hWalls = mapField.getHWalls();
        boolean[][] vWalls = mapField.getVWalls();

        for (int row = 0; row < rowCount; row++) {
            // render horizontal walls
            renderHorizontalWallLine(hWalls, row, colCount);

            // render vertical walls and tiles
            for (int col = 0; col < colCount; col++) {

                // left vertical wall
                if (vWalls[row][col]) {
                    console.print('|');
                } else {
                    console.print(' ');
                }

                Tile tile = tiles[row][col];
                char c = getCharByTileType(tile.getType());
                if (col == px && row == py) {
                    c = playerCh;
                }
                console.print(c + " ");
            }

            // right outer wall
            if (vWalls[row][colCount]) {
                console.print('|');
            }

            console.print('\n');
        }

        // bottom horizontal line
        renderHorizontalWallLine(hWalls, rowCount, colCount);
    }

    private char getCharByTileType(TileType tileType) {
        return switch (tileType) {
            case PLAYER_SPAWN, CLEAR -> ' ';
            case TARGET -> '✿';
        };
    }
}
