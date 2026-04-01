package sk.tuke.gamestudio.game.logicalmazes.ui.console.pages;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.ConsoleRenderer;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.InputHelper;

@Profile("console")
@Component
public class AboutPage {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final InputHelper inputHelper;

    public AboutPage(Console console, ConsoleRenderer consoleRenderer, InputHelper inputHelper) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
        this.inputHelper = inputHelper;
    }

    public void show() {
        console.clear();
        consoleRenderer.renderFromFile("ui/console/uiTexts/about_title.txt");
        int x = 10;
        consoleRenderer.renderFromFile("ui/console/uiTexts/about_text.txt", x, 12, false, Game.version, Game.author);
        inputHelper.waitForConfirm("Back", x, 20);
    }
}