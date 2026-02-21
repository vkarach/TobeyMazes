package sk.tuke.gamestudio.game.logicalmazes.console;

import sk.tuke.gamestudio.game.logicalmazes.core.Field;
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

    public void drawGame(Field mapField) {
//        char playerCh = '♞';

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
            case PLAYER_SPAWN      -> 'A';
            case DESTINATION       -> '!';
            case CLEAR             -> ' ';
        };
    }
}
