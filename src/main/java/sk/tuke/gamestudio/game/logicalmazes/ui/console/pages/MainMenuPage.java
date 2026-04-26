package sk.tuke.gamestudio.game.logicalmazes.ui.console.pages;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;
import sk.tuke.gamestudio.game.logicalmazes.ui.MenuOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.ConsoleRenderer;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.KonekTobeyAnimation;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Selector;

@Profile("console")
@Component
public class MainMenuPage {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final Selector selector;

    public MainMenuPage(Console console, ConsoleRenderer consoleRenderer, Selector selector) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
        this.selector = selector;
    }

    public MenuOption show() {
        console.clear();

        String versionText = Game.versionLabel + Game.version;
        console.print(versionText, console.getWidth() - versionText.length(), console.getHeight() - 1);

        consoleRenderer.renderFromFile("ui/console/uiTexts/game_title.txt");
        Thread anim = new KonekTobeyAnimation(console, consoleRenderer).startKonekTobeyAnimation(80, 20);

        MenuOption[] actions = {
            MenuOption.START,
            MenuOption.PROFILE,
            MenuOption.LEADERBOARD,
            MenuOption.RATE,
            MenuOption.ABOUT,
            MenuOption.EXIT,
        };

        MenuOption result = selector.select(actions);

        anim.interrupt();
        try {
            anim.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result == null ? MenuOption.EXIT : result;
    }
}