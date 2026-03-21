package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.AuthService;

@Component
public class AuthConsole {
    private final Console console;
    private final AuthService authService;
    private final ConsoleRenderer consoleRenderer;
    private final Notifier notifier;
    private final InputHelper inputHelper;

    public AuthConsole(Console console, AuthService authService, ConsoleRenderer consoleRenderer, InputHelper inputHelper, Notifier notifier) {
        this.console = console;
        this.authService = authService;
        this.consoleRenderer = consoleRenderer;
        this.notifier = notifier;
        this.inputHelper = inputHelper;
    }

    private String readValidInput(String prompt, int x, int y) {
        String regex = "^[A-Za-z0-9_-]+$";
        return readValidInput(prompt, regex, 3, 16, x, y);
    }

    private String readValidInput(String prompt, String regex, int minLen, int maxLen, int x, int y) {
        String input = inputHelper.getUserInput(prompt, x, y);
        if (input == null) {
            return null;
        }
        String inputError = inputHelper.validateInput(input, regex,minLen, maxLen);
        if (inputError != null) {
            notifier.showError(inputError, x, y + 2);
            return readValidInput(prompt, regex, minLen, maxLen, x, y);
        }
        return input;
    }

    private String[] getNamePassword(int x, int y) {
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
            return new String[] { name, password };
        }
    }

    public User register() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/register.txt");

        int x = 30, y = 20;

        String[] namePassword = getNamePassword(x, y);
        y += 4;

        if (namePassword == null) { // interrupted by a user
            return null;
        }
        String name = namePassword[0];
        String password = namePassword[1];

        if (authService.userNameTaken(name)) {
            notifier.showError("﹂ User name already in use", x, y);
            return register();
        }

        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        String email = readValidInput("email: ", regex, 5, 60, x, y);
        if (email == null) return register(); // ???

        Thread loadAnim = consoleRenderer.renderAnimation("animations/loading.txt", 75, x, ++y);

        if (authService.emailTaken(email)) {
            loadAnim.interrupt();
            notifier.showError("﹂ EmailVerification already in use", x, y);
            return register();
        }

        int code = authService.sendOrGetVerificationCodeByEmail(email);

        loadAnim.interrupt();
        console.clearLine(10, x, y);

        console.print("We've sent a verification code to your email.", x, ++y);

        console.print("Enter the code below to confirm password change.", x, ++y);

        y+=2;
        if (!verifyCode(code, x, y)) return register(); // ???

        for (int i = 0; i < 10; i++) {
            console.clearLine(100, x, y + i);
        }

        loadAnim = consoleRenderer.renderAnimation("animations/loading.txt", 75, x, y);

        User user = authService.register(name, password, email);

        loadAnim.interrupt();
//        if (user == null) {
//            notifier.showError("User with this name already exists", x, y);
//            register();
//            console.enterRawMode();
//            return null;
//        }

        authService.expireEmail(email);

        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)).append(user.getName());
        sb.style(AttributedStyle.DEFAULT).append(" now you registered!");
        console.print(sb, x, y);

        inputHelper.waitForConfirm("Refresh", x, y + 2);

        return user;
    }

    public User login() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/login.txt");

        int x = 30, y = 20;

        String[] namePassword = getNamePassword(x, y);
        for (int i = 0; i < 5; i++) {
            console.clearLine(x, y + i);
        }
        if (namePassword == null) { // interrupted by a user
            return null;
        }
        String name = namePassword[0];
        String password = namePassword[1];

        Thread loadAnim = consoleRenderer.renderAnimation("animations/loading.txt", 75, x, y);

        User user = authService.login(name, password);

        loadAnim.interrupt();
        if (user == null) {
            notifier.showError("Wrong name or password", x, y);
            return login();
        }

        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)).append(user.getName());
        sb.style(AttributedStyle.DEFAULT).append(", love to see ya again :)");
        console.print(sb, x, y);

        inputHelper.waitForConfirm("Refresh", x, y + 2);

        return user;
    }

    public void changePassword(int userId) {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/change_password.txt");

        int x = 20, y = 20;

        console.print("We've sent a verification code to your email.", x, y++);

        console.print("Enter the code below to confirm password change.", x, y);
        y+=2;

        Thread loadAnim = consoleRenderer.renderAnimation("animations/loading.txt", 75, x, y);
        int code = authService.getOrCreateEmailVerificationCode(userId);
        loadAnim.interrupt();

        if (!verifyCode(code, x, y++)) return;

        String newPassword = readValidInput("New password: ", x, y);
        if (newPassword == null) return;
        y+=2;

        authService.changePassword(userId, newPassword);

        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)).append("✓");
        sb.style(AttributedStyle.DEFAULT).append(" Password successfully changed");
        console.print(sb, x, y);
        y+=2;

        authService.expireEmailByUserId(userId);

        inputHelper.waitForConfirm("Back", x, y + 1);
    }

    private boolean verifyCode(int code, int x, int y) {
        while (true) {
            String input = inputHelper.getUserInput("Code: ", x, y);
            if (input == null) {
                return false;
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
        return true;
    }

}
