package sk.tuke.gamestudio.game.logicalmazes.ui.console.pages;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.ConsoleRenderer;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.InputHelper;

import java.time.Duration;

@Component
public class WinPage {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final InputHelper inputHelper;

    public WinPage(Console console, ConsoleRenderer consoleRenderer, InputHelper inputHelper) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
        this.inputHelper = inputHelper;
    }

    public void show(long playedTimeMs, int points, boolean isTimeRecord, boolean isScoreRecord) {
        Duration duration = Duration.ofMillis(playedTimeMs);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).toSeconds();
        long millis  = duration.minusMinutes(minutes).minusSeconds(seconds).toMillis() / 10;

        console.clear();
        consoleRenderer.renderFromFile("uiTexts/level_complete.txt");

        int x = 10;
        int y = 20;

        consoleRenderer.renderFromFile("uiTexts/megamind.txt", 50, y);

        console.print("+---------------------------+", x, y++);
        console.print("|        MEGA   MIND        |", x, y++);
        console.print("+---------------------------+", x, y++);
        y++;

        AttributedStyle numbersStyle = AttributedStyle.DEFAULT.foreground(141);
        AttributedStyle recordStyle  = AttributedStyle.DEFAULT.foreground(220);

        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.append("| Time: ");
        if (minutes > 0) sb.style(numbersStyle).append(String.valueOf(minutes)).append(":");
        sb.style(numbersStyle).append(String.format("%02d:%02d", seconds, millis));
        console.print(sb, x, y++);

        sb = new AttributedStringBuilder();
        sb.append("| Points: ").style(numbersStyle).append(String.valueOf(points));
        console.print(sb, x, y++);
        y++;

        if (isScoreRecord) {
            sb = new AttributedStringBuilder();
            sb.append("| ").style(recordStyle).append("NEW SCORE RECORD!");
            console.print(sb, x, y++);
        }
        if (isTimeRecord) {
            sb = new AttributedStringBuilder();
            sb.append("| ").style(recordStyle).append("BEAT YOUR BEST TIME!");
            console.print(sb, x, y++);
        }
        y++;
        console.print("+---------------------------+", x, y++);
        inputHelper.waitForConfirm("Back", x, y);
    }
}