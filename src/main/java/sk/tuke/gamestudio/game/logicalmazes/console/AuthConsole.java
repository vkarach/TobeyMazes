package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.AuthService;
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

    private String readValidInput(String prompt, int x, int y) {
        String input = inputHelper.getUserInput(prompt, x, y);
        if (input == null) {
            return null;
        }
        String inputError = inputHelper.validateInput(input, 3, 16);
        if (inputError != null) {
            notifier.showError(inputError, x, y + 2);
            return readValidInput(prompt, x, y);
        }
        return input;
    }

    private String[] getNamePassword() {
        int x = 30, y = 20;
        while (true) {
            console.clearLine(x, y + 2);

            String name = readValidInput("your name (Ctrl+D exit): ", x, y);
            if (name == null) {
                return null;
            }

            String password = readValidInput("password  (Ctrl+D back): ", x, y + 2);
            if (password == null) {
                continue;
            }
            for (int i = 0; i < 5; i++) {
                console.clearLine(x, y + i);
            }
            return new String[] { name, password };
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
        Thread loadAnim = consoleRenderer.renderAnimation("animations/loading.txt", 50, x, y); // can overwrite user input need to check

        User user = authService.register(name, password);

        loadAnim.interrupt();
        if (user == null) {
            notifier.showError("User with this name already exists", x, y);
            register();
            console.enterRawMode();
            return null;
        }

        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)).append(user.name());
        sb.style(AttributedStyle.DEFAULT).append(" now you registered!");
        console.print(sb, x, y);

        waitForInput("Refresh", x, y + 2);

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

        int x = 30, y = 20;
        Thread loadAnim = consoleRenderer.renderAnimation("animations/loading.txt", 50, x, y);

        User user = authService.login(name, password);

        loadAnim.interrupt();
        if (user == null) {
            notifier.showError("Wrong name or password", x, y);
            login();
            return null;
        }

        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)).append(user.name());
        sb.style(AttributedStyle.DEFAULT).append(", love to see ya again :)");
        console.print(sb, x, y);

        waitForInput("Refresh", x, y + 2);

        return user;
    }

    public void changePassword(int userId) {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/change_password.txt");

        int x = 20, y = 20;

        console.print("We've sent a code to your email ()", x, y++); // todo: email here
        console.print("Print code below to change password", x, y++);

        verifyEmail(userId, x, y++);


        String newPassword = readValidInput("New password: ", x, y++);

        authService.changePassword(userId, newPassword);

        console.print("Password successfully changed", x, y++);

        waitForInput("Back", x, y + 1);
    }

    private void verifyEmail(int userId, int x, int y) {
        Thread loadAnim = consoleRenderer.renderAnimation("animations/loading.txt", 50, x, y);

        int code = authService.getCode(userId);

        loadAnim.interrupt();
        while (true) {
            String input = inputHelper.getUserInput("Code: ", x, y);
            if (input == null) {
                return;
            }

            if (!input.matches("\\d{6}")) {
                notifier.showError("Code must contain exactly 6 digits", x, y + 2);
                continue;
            }

            if (code != Integer.parseInt(input)) {
                notifier.showError("Wrong code", x, y + 2);
                continue;
            }
            break;
        }
    }

    private void waitForInput(String text, int x, int y) {
        console.print("▶ " + text,
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
