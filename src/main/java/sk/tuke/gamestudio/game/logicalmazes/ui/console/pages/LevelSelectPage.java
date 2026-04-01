package sk.tuke.gamestudio.game.logicalmazes.ui.console.pages;

import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.BestLevelResult;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.game.logicalmazes.ui.LevelOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.ConsoleRenderer;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Selector;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.List;
import java.util.Random;

@Profile("console")
@Component
public class LevelSelectPage {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final Selector selector;
    private final BestResultService bestResultService;

    public LevelSelectPage(Console console, ConsoleRenderer consoleRenderer, Selector selector, BestResultService bestResultService) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
        this.selector = selector;
        this.bestResultService = bestResultService;
    }

    public Level show(User currentUser) {
        console.clear();
        consoleRenderer.renderFromFile("ui/console/uiTexts/konek_tobey_big.txt", 75, 0);

        String[] files = {
            "ui/console/uiTexts/frames/hardcore_frame.txt",
            "ui/console/uiTexts/frames/ready_frame.txt",
            "ui/console/uiTexts/frames/u_got_this_frame.txt",
            "ui/console/uiTexts/frames/braining_frame.txt",
        };
        Random random = new Random();

        Thread anim = null;
        if (random.nextInt(20) == 0) {
            anim = consoleRenderer.renderAnimation("ui/console/uiTexts/frames/brosky_anim.txt", 50, 127, 8);
        } else {
            consoleRenderer.renderFromFile(files[random.nextInt(files.length)], 127, 8);
        }

        final String[] scrollStart = {
            "  _________________________________________  ",
            "=(__    ___    ____   __     ___    ___   _)=",
            "  |                                       |  "
        };
        final String[] scrollEnd = {
            "  |                                       |",
            "  |__  __   ___     __    __     ___   ___|",
            "=(_________________________________________)="
        };

        consoleRenderer.renderFromFile("ui/console/uiTexts/select_level.txt");
        LevelOption[] options = buildLevelOption(currentUser);

        int x = Selector.DEFAULT_X;
        int y = 24;

        if (currentUser == null) {
            console.print("Sign in to save your score and time", x - 2, y - scrollStart.length - 1);
        }

        for (int i = 0; i < options.length; i++) {
            console.print("|", x - 2, y + i);
        }
        consoleRenderer.renderStringList(scrollStart, x - 4, y - scrollStart.length);
        consoleRenderer.renderStringList(scrollEnd,   x - 4, y + options.length);
        console.print(String.format("%-12s %-8s %-8s %-6s", "Level", "Diff", "Score", "Time"),
                x, y - 1, AttributedStyle.DEFAULT.foreground(245));

        LevelOption selected = selector.select(options, "", x, y);

        if (anim != null) anim.interrupt();

        if (selected == null || selected == LevelOption.BACK) return null;
        return selected.getLevel();
    }

    private LevelOption[] buildLevelOption(User currentUser) {
        List<BestLevelResult> bestResultsByUser = currentUser != null
                ? bestResultService.getBestResultsByUserId(currentUser.getId())
                : null;

        final int titleWidth = 39;
        LevelOption[] options = LevelOption.values();

        for (LevelOption option : options) {
            if (option.getLevel() == null) {
                option.setTitle(String.format("%-" + (titleWidth - 1) + "s|", "Back"));
                continue;
            }

            String levelTitle = option.getLevel().getTitle();
            String dif = coloredDifficulty(option.getLevel().getDifficulty());
            int escLen = dif.length() - option.getLevel().getDifficulty().toString().length();

            if (currentUser != null) {
                String scoreStr = "---";
                String timeStr  = "---";
                for (BestLevelResult br : bestResultsByUser) {
                    if (br.getId().getLevelId() == option.getLevel().getId()) {
                        scoreStr = "" + br.getBestScore();
                        timeStr  = formatBestTime(br.getBestTimeMs());
                        break;
                    }
                }
                option.setTitle(String.format("%-12s %-" + (8 + escLen) + "s %-8s %-6s |", levelTitle, dif, scoreStr, timeStr));
            } else {
                option.setTitle(String.format("%-12s %-" + (8 + escLen) + "s %-8s %-6s |", levelTitle, dif, "---", "---"));
            }
        }
        return options;
    }

    private String coloredDifficulty(Level.Difficulty difficulty) {
        String escColor = switch (difficulty) {
            case EASY   -> "\033[32m";
            case NORMAL -> "\033[38;5;33m";
            case MEDIUM -> "\033[33m";
            case HARD   -> "\033[31m";
        };
        return escColor + difficulty + "\033[39m";
    }

    private String formatBestTime(Long bestTimeMs) {
        if (bestTimeMs == null) return "N/A";
        return String.format("%d:%02d", bestTimeMs / 1000, (bestTimeMs % 1000) / 10);
    }
}