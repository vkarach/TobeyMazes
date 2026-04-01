package sk.tuke.gamestudio.game.logicalmazes.ui.console;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.*;
import sk.tuke.gamestudio.game.logicalmazes.ui.LevelView;

@Profile("console")
@Component
public class LevelUI implements LevelView {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;

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

    private Field gameField;
    private Player player;
    private int x;
    private int y;
    private int hudX;

    public LevelUI(Console console, ConsoleRenderer consoleRenderer) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
    }

    private void renderHorizontalWallLine(boolean[][] hWalls, int rowIndex, int colCount, int x, int y) {
        char crossCh = '+';
        AttributedStringBuilder sb = new AttributedStringBuilder();
        for (int col = 0; col < colCount; col++) {
            sb.style(crossWallStyle).append(crossCh);
            if (hWalls[rowIndex][col]) {
                sb.style(wallStyle).append("--");
            } else {
                sb.style(AttributedStyle.DEFAULT).append("  ");
            }
        }
        sb.style(crossWallStyle).append(crossCh);
        console.print(sb, x, y);
    }

    private String formatTimerString(long startTime) {
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

    public void launchLevel(Field field) {
        this.gameField = field;
        this.x = (console.getWidth() / 2) - (field.getRowCount() * 3);
        this.y = 20;
        this.hudX = x + this.gameField.getRowCount() * 3 + 5;

        console.clear();
        consoleRenderer.renderFromFile("ui/console/uiTexts/game_title.txt");

        int lowerBoundPad = gameField.getRowCount() * 2;
        int konekTobeyPadY = lowerBoundPad - consoleRenderer.getRenderFromFileSize("ui/console/uiTexts/konek_tobey.txt").height() + 1;
        consoleRenderer.renderFromFile("ui/console/uiTexts/konek_tobey.txt", hudX, y + konekTobeyPadY, true);
    }

    public void updateHud(long startTime, int targetCount, int points) {
        int curY = this.y;
        String vBound = "+" + "-".repeat(11) + "+";
        console.print(vBound, hudX, curY++, wallStyle);

        String[] toPrint = new String[] {
            String.format(" %-10s", formatTimerString(startTime)),
            String.format(" %-10s", "🏆 " + points),
            String.format(" %-10s", " " + targetCount + ' ' + targetCh)
        };

        for (String s : toPrint) {
            AttributedStringBuilder sb = new AttributedStringBuilder();
            sb.style(wallStyle).append('|');
            sb.style(textStyle).append(s);
            sb.style(wallStyle).append('|');
            console.print(sb, hudX, curY++);
        }
        console.print(vBound, hudX, curY, wallStyle);
    }

    public void renderTips() {
        int tipsY = this.y + gameField.getRowCount() * 2 + 2;
        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.style(crossWallStyle).append("←↓↑→");
        sb.style(textStyle).append(" move · ");
        sb.style(crossWallStyle).append("Q");
        sb.style(textStyle).append(" - Quit · ");
        sb.style(crossWallStyle).append("R");
        sb.style(textStyle).append(" - Restart");
        console.print(sb, x, tipsY);
    }

    public void renderField(Field field, Player player) {
        console.moveCursorToStart();
        final char playerCh = '♞';

        int px = player.getX();
        int py = player.getY();

        int rowCount = field.getRowCount();
        int colCount = field.getColCount();

        Tile[][] tiles = field.getTiles();
        boolean[][] hWalls = field.getHWalls();
        boolean[][] vWalls = field.getVWalls();

        for (int row = 0; row < rowCount; row++) {
            int yHoriz = y + row * 2;
            int yCells = y + row * 2 + 1;

            // horizontal walls line
            renderHorizontalWallLine(hWalls, row, colCount, x, yHoriz);

            // vertical walls + tiles line
            AttributedStringBuilder sb = new AttributedStringBuilder();

            for (int col = 0; col < colCount; col++) {
                if (vWalls[row][col]) {
                    sb.style(wallStyle).append('|');
                } else {
                    sb.style(AttributedStyle.DEFAULT).append(' ');
                }

                Tile tile = tiles[row][col];

                char c;
                AttributedStyle style;
                if (col == px && row == py) {
                    c = playerCh;
                    style = playerStyle;
                } else if (tile.getType() == TileType.TARGET) {
                    c = targetCh;
                    style = targetStyle;
                } else {
                    c = ' ';
                    style = textStyle;
                }

                sb.style(style).append(c);
                sb.style(AttributedStyle.DEFAULT).append(' ');
            }

            // right outer wall
            if (vWalls[row][colCount]) {
                sb.style(wallStyle).append('|');
            } else {
                sb.style(AttributedStyle.DEFAULT).append(' ');
            }

            console.print(sb, x, yCells);
        }

        // bottom horizontal line
        renderHorizontalWallLine(hWalls, rowCount, colCount, x, y + rowCount * 2);
    }
}
