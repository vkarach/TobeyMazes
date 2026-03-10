package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.AuthService;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;

public class AuthConsole {
    private final Console console;
    private final AuthService authService;
    private final ConsoleRenderer consoleRenderer;
    private final Notifier notifier;
    private final InputHelper inputHelper;

    public AuthConsole(Console console, AuthService authService) {
        this.console = console;
        this.authService = authService;
        this.consoleRenderer = new ConsoleRenderer(console);
        this.notifier = new Notifier(console);
        this.inputHelper = new InputHelper(console);
    }

    private String[] getNamePassword() {
        int x = 30, y = 20;
        while (true) {
            String name = inputHelper.getUserInput("your name (Ctrl+D exit): ", x, y);
            if (name == null) { // interrupted by a user
                console.enterRawMode();
                return null;
            }
            String nameErrorMsg = inputHelper.validateInput(name, 3, 16);
            if (nameErrorMsg != null) {
                notifier.showError(nameErrorMsg, x, y + 2);
                continue;
            }

            while (true) {
                String password = inputHelper.getUserInput("password  (Ctrl+D back): ", x, y);
                if (password == null) {
                    break; // back to name input
                }
                String passwordErrorMsg = inputHelper.validateInput(password, 3, 16);
                if (passwordErrorMsg != null) {
                    notifier.showError(passwordErrorMsg, x, y + 2);
                    continue;
                }
                return new String[] { name, password };
            }
        }
    }

    public User register() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/register.txt");

        String[] namePassword = getNamePassword(); // todo password
        if (namePassword == null) { // interrupted by a user
            return null;
        }

        String name = namePassword[0];

        console.print("loading...", 20, 20);

        User user = authService.register(name);
        if (user == null) {
            console.enterRawMode();
            return null;
        }

        console.print(user.getName() + " now you registered!", 20, 20);

        waitForRefresh(20, 22);

        return user;
    }

    public User login() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/login.txt");

        String[] namePassword = getNamePassword(); // todo password
        if (namePassword == null) { // interrupted by a user
            return null;
        }
        String name = namePassword[0];

        console.print("loading...", 20, 20);

        User user = authService.login(name);
        if (user == null) {
            return null;
        }

        console.print(user.getName() + ", love to see ya again :)", 20, 20);

        waitForRefresh(20, 22);

        return user;
    }

    private void waitForRefresh(int x, int y) {
        console.print("▶ Refresh",
                x, y,
                AttributedStyle.DEFAULT.background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK)
        );

        while (true) {
            InputType input = console.readAction();
            if (input == InputType.ENTER || input == InputType.QUIT) {
                return;
            }
        }
    }
}
