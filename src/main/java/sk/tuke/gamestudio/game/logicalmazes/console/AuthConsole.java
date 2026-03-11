package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStringBuilder;
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

        String[] namePassword = getNamePassword();
        if (namePassword == null) { // interrupted by a user
            return null;
        }

        String name = namePassword[0];
        String password = namePassword[1];

        int x = 20, y = 20;
        console.print("loading...", x, y);

        User user = authService.register(name, password);
        if (user == null) {
            notifier.showError("User with this name already exists", x, y);
            register();
            console.enterRawMode();
            return null;
        }

        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)).append(user.getName());
        sb.style(AttributedStyle.DEFAULT).append(" now you registered!");
        console.print(sb, x, y);

        waitForRefresh(x, y + 2);

        return user;
    }

    public User login() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/login.txt");

        String[] namePassword = getNamePassword();
        if (namePassword == null) { // interrupted by a user
            return null;
        }
        String name = namePassword[0];
        String password = namePassword[1];

        int x = 20, y = 20;
        console.print("loading...", x, y);

        User user = authService.login(name, password);
        if (user == null) {
            notifier.showError("Wrong name or password", x, y);
            login();
            return null;
        }

        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)).append(user.getName());
        sb.style(AttributedStyle.DEFAULT).append(", love to see ya again :)");
        console.print(sb, x, y);

        waitForRefresh(x, y + 2);

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
