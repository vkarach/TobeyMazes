package sk.tuke.gamestudio.game.logicalmazes.ui.console.pages;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.ui.ProfileOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.ConsoleRenderer;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Selector;
import sk.tuke.gamestudio.service.BestResultService;

@Component
public class ProfilePage {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final Selector selector;
    private final BestResultService bestResultService;

    public ProfilePage(Console console, ConsoleRenderer consoleRenderer, Selector selector, BestResultService bestResultService) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
        this.selector = selector;
        this.bestResultService = bestResultService;
    }

    public ProfileOption showGuest() {
        console.clear();
        consoleRenderer.renderFromFile("uiTexts/login_or_register.txt");

        ConsoleRenderer.RenderSize size = consoleRenderer.getRenderFromFileSize("uiTexts/who_are_you.txt");
        consoleRenderer.renderFromFile("uiTexts/who_are_you.txt",
                console.getWidth() - size.width() - 20, console.getHeight() - size.height());

        ProfileOption[] options = {ProfileOption.REGISTER, ProfileOption.LOGIN, ProfileOption.BACK};
        return selector.select(options);
    }

    public ProfileOption showAuthorized(User user) {
        console.clear();
        consoleRenderer.renderFromFile("uiTexts/your_profile.txt");

        Integer bestScore = bestResultService.getBestOverallScore(user.getId());
        String horzBound = "+" + "-".repeat(25) + "+";
        String name  = String.format("Name: %s", user.getName());
        String score = String.format("Overall score: %d", bestScore != null ? bestScore : 0);

        consoleRenderer.renderFromFile("uiTexts/konek_tobey_big.txt", 75, 0);
        consoleRenderer.renderFromFile("uiTexts/frames/hello_there_frame.txt", 127, 8);

        int x = 20, y = 20;
        console.print(horzBound, x, y++);
        console.print(String.format("| %-23s |", name),  x, y++);
        console.print(String.format("| %-23s |", score), x, y++);
        console.print(horzBound, x, y);

        ProfileOption[] options = {ProfileOption.LOGOUT, ProfileOption.CHANGE_PASSWORD, ProfileOption.BACK};
        return selector.select(options, x, 25);
    }
}