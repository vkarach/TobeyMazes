package sk.tuke.gamestudio.game.logicalmazes.ui.console.pages;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.ConsoleRenderer;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.InputHelper;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Selector;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.List;

@Component
public class LeaderboardPage {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final BestResultService bestResultService;
    private final InputHelper inputHelper;

    public LeaderboardPage(Console console, ConsoleRenderer consoleRenderer, BestResultService bestResultService, InputHelper inputHelper) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
        this.bestResultService = bestResultService;
        this.inputHelper = inputHelper;
    }

    public void show(User user) {
        Integer curUserId = user != null ? user.getId() : null;
        console.clear();

        final String[] scrollStart = {
            "  ________________________________  ",
            "=(__    ___    ___   __    ___   _)=",
            "  |                              |  "
        };
        final String[] scrollEnd = {
            "  |                              |",
            "  |__  __   ___     __    __  ___|",
            "=(________________________________)="
        };

        consoleRenderer.renderFromFile("uiTexts/leaderboard.txt");

        ConsoleRenderer.RenderSize size = consoleRenderer.getRenderFromFileSize("uiTexts/trophy.txt");
        consoleRenderer.renderFromFile("uiTexts/trophy.txt", 85, console.getHeight() - size.height());

        List<UserScore> topUserScores = bestResultService.getTopByScore();
        if (topUserScores.isEmpty()) {
            console.print("No one here yet :(", 30, 13);
            inputHelper.waitForConfirm("Back", Selector.DEFAULT_X, 15);
            return;
        }

        String bestUserName = topUserScores.getFirst().userName();
        int leftPadding = (24 - bestUserName.length()) / 2;
        console.print("The best of the best", 98, console.getHeight() - 6);
        console.print(bestUserName, 95 + leftPadding, console.getHeight() - 4,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));

        int x = 40;
        int y = 15;

        consoleRenderer.renderStringList(scrollStart, x - 2, y);
        y += scrollStart.length;

        AttributedStyle[] topColors = {
            AttributedStyle.DEFAULT.foreground(220),
            AttributedStyle.DEFAULT.foreground(159),
            AttributedStyle.DEFAULT.foreground(130),
        };

        int idx = 0;
        for (UserScore score : topUserScores) {
            AttributedStringBuilder sb = new AttributedStringBuilder();
            sb.append("| ");

            AttributedStyle style = idx < topColors.length ? topColors[idx] : AttributedStyle.DEFAULT;
            if (curUserId != null && score.userId() == curUserId) style = style.italic().bold();

            sb.style(style).append(String.format("%02d: %-16s", idx + 1, score.userName()));
            sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
              .append(String.format(" %7d |", score.totalScore()));

            console.print(sb, x, y++);
            idx++;
        }

        consoleRenderer.renderStringList(scrollEnd, x - 2, y);
        y += scrollEnd.length;
        inputHelper.waitForConfirm("Back", x, y + 2);
    }
}