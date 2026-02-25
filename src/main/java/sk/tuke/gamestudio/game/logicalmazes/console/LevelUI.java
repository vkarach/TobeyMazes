package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.game.logicalmazes.core.Field;
import sk.tuke.gamestudio.game.logicalmazes.core.Player;
import sk.tuke.gamestudio.game.logicalmazes.core.Tile;
import sk.tuke.gamestudio.game.logicalmazes.core.TileType;

public class LevelUI {
    private final Console console;

//    private final AttributedStyle wallStyle =
//            AttributedStyle.DEFAULT
//                    .foreground(AttributedStyle.BRIGHT | AttributedStyle.CYAN);

    private final AttributedStyle wallStyle =
        AttributedStyle.DEFAULT.foreground(103);

    private final AttributedStyle crossWallStyle =
            AttributedStyle.DEFAULT.foreground(141);

    private final AttributedStyle textStyle =
            AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);

    private final AttributedStyle targetStyle =
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW);

    private final AttributedStyle playerStyle = textStyle;
//            AttributedStyle.DEFAULT.foreground(94);


    private final char targetCh = '✿';

    public LevelUI(Console console) {
        this.console = console;
    }

    private void renderHorizontalWallLine(boolean[][] hWalls, int rowIndex, int colCount) {
        for (int col = 0; col < colCount; col++) {
            console.print("+", crossWallStyle); // todo: "+" <-- wall style or other???
            if (hWalls[rowIndex][col]) {
                console.print("--", wallStyle);
            } else {
                console.print("  ");
            }
        }
        console.print("+", crossWallStyle);
        console.print('\n');
    }

    private String getTimerString(long startTime) {
        final long nowNs = System.nanoTime();
        long durationMs = (nowNs - startTime) / 1_000_000L;

        if (durationMs < 0) durationMs = 0;

        final long totalSeconds = durationMs / 1000L;
        final long minutes = totalSeconds / 60L;
        final long seconds = totalSeconds % 60L;

        long ms = (durationMs % 1000L) / 10L;

        final String[] clocks = new String[] {
                "\uD83D\uDD50", // 1:00
                "\uD83D\uDD51", // 2:00
                "\uD83D\uDD52", // 3:00
                "\uD83D\uDD53", // 4:00
                "\uD83D\uDD54", // 5:00
                "\uD83D\uDD55", // 6:00
                "\uD83D\uDD56", // 7:00
                "\uD83D\uDD57", // 8:00
                "\uD83D\uDD58", // 9:00
                "\uD83D\uDD59", // 10:00
                "\uD83D\uDD5A", // 11:00
                "\uD83D\uDD5B"  // 12:00
        };
        String clock = clocks[(int) seconds % clocks.length];

        if (minutes > 0) {
            return String.format("%s %d:%02d", clock, minutes, seconds);
        } else {
            return String.format("%s %d:%02d", clock, totalSeconds, ms);
        }
    }

    public void renderHud(long startTime, int targetCount, int x, int y) {
        String timeString     = String.format(" %-10s", getTimerString(startTime));
        String targetCountStr = String.format(" %-10s", "   " + (targetCount > 0 ? targetCount: '✓') + ' ' + targetCh);

        String vBound = "+" + "-".repeat(11) + "+";

        console.print(vBound, x, y, wallStyle); y++;

        AttributedStringBuilder asb = new AttributedStringBuilder();
        asb.style(wallStyle).append('|');
        asb.style(textStyle).append(timeString);
        asb.style(wallStyle).append('|');
        console.print(asb, x, y); y++;

        asb = new AttributedStringBuilder();
        asb.style(wallStyle).append('|');
        asb.style(textStyle).append(targetCountStr);
        asb.style(wallStyle).append('|');
        console.print(asb, x, y); y++;

        console.print(vBound, x, y, wallStyle);
    }

    public void renderGameField(Field mapField, Player player, boolean clear) {
        if (clear) {
            console.moveCursorToStart();
        }
        renderGameField(mapField, player);
    }

    public void renderGameField(Field mapField, Player player) {
        final char playerCh = '♞';
//        final char targetCh = '✿';

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
                    console.print("|", wallStyle);
                }
                else {
                    console.print(' ');
                }

                Tile tile = tiles[row][col];

                char c;
                AttributedStyle style;
                if (col == px && row == py) {
                    c = playerCh;
                    style = playerStyle;
                }
                else if (tile.getType() == TileType.TARGET) {
                    c = targetCh;
                    style = targetStyle;
                }
                else {
                    c = ' ';
                    style = textStyle;
                }
                console.print(c + " ", style);
            }

            // right outer wall
            if (vWalls[row][colCount]) {
                console.print("|", wallStyle);
            }

            console.print('\n');
        }

        // bottom horizontal line
        renderHorizontalWallLine(hWalls, rowCount, colCount);
    }
    // play
    // >/▶/➤/▸/» settings or A_REVERSE A_BOLD
    // records
}
