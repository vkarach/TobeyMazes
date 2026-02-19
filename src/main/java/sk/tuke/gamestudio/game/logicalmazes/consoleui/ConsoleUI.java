package sk.tuke.gamestudio.game.logicalmazes.consoleui;

import sk.tuke.gamestudio.game.logicalmazes.core.Field;
import sk.tuke.gamestudio.game.logicalmazes.core.Tile;
import sk.tuke.gamestudio.game.logicalmazes.core.TileType;

public class ConsoleUI {
    private final Field mapField;

    public ConsoleUI(Field mapField) {
        this.mapField = mapField;
    }

    public void drawField() {
        int rowCount = mapField.getRowCount();
        int colCount = mapField.getColCount();

        StringBuilder sb = new StringBuilder();
        sb.append("\033[2J\033[H"); // clear console and put cursor on the top

        sb.append('|').
                append("-".repeat(colCount * 2)).
                append('|'); // upper bound

        sb.append('\n');

        Tile[][] tiles = mapField.getTiles();
        for (int row = 0; row < rowCount; row++ ) {
            sb.append('|');
            for (int col = 0; col < colCount; col++ ) {
                Tile tile = tiles[row][col];
                char c = getCharByTileType(tile.getType());

                sb.append(c).append(' ');
            }
            sb.append('|');
            sb.append('\n');
        }

        sb.append('|').
                append("-".repeat(colCount * 2))
                .append('|'); // lower bound

        sb.append("\n\n");

        System.out.print(sb); // drop built string
    }
    private char getCharByTileType(TileType tileType) {
        return switch (tileType) {
            case CLEAR             -> ' ';
            case HORIZONTAL_WALL   -> '_';
            case VERTICAL_WALL     -> '|';
            case PLAYER_SPAWN      -> 'A';
            case DESTINATION       -> '!';
        };
    }
}
