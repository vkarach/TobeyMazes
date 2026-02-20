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
    public void drawGame(Field mapField, Player player) {
        drawGame(mapField, player, false);
    }
    public void drawGame(Field mapField, Player player, boolean clear) {
        if (clear) {
            console.clear();
        }
        int rowCount = mapField.getRowCount();
        int colCount = mapField.getColCount();

        char vBoundChr = '│';
        char hBoundChr = '─';
        char playerCh = '♞';

        String horizontalBound = String.valueOf(hBoundChr).repeat(colCount * 2);

        StringBuilder sb = new StringBuilder();

        sb.append(vBoundChr);
        sb.append(horizontalBound);
        sb.append(vBoundChr);

        sb.append('\n');

        Tile[][] tiles = mapField.getTiles();
        for (int row = 0; row < rowCount; row++ ) {
            sb.append(vBoundChr);
            for (int col = 0; col < colCount; col++ ) {
                Tile tile = tiles[row][col];
                char c = getCharByTileType(tile.getType());

                if (row == player.getX() && col == player.getY()) {
                    sb.append(playerCh);
                }
                else {
                    sb.append(c);
                }
                sb.append(' ');
            }
            sb.append(vBoundChr);
            sb.append('\n');
        }

        sb.append(vBoundChr);
        sb.append(horizontalBound);
        sb.append(vBoundChr); // lower bound

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
