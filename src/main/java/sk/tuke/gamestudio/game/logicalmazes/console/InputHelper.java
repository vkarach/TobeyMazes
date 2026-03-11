package sk.tuke.gamestudio.game.logicalmazes.console;

public class InputHelper {
    private final Console console;

    public InputHelper(Console console) {
        this.console = console;
    }

    public String getUserInput(String prompt, int x, int y) {
        int clearLen = 80;

        console.setCursorPosition(x, y);

        console.exitRawMode();

        console.print(" ".repeat(clearLen), x, y);
        console.setCursorPosition(x, y);

        String input = console.readLine(prompt);
        if (input == null) {
            console.enterRawMode();
            return null;
        }

        console.print(" ".repeat(clearLen), x, y);
        console.setCursorPosition(x, y);

        console.enterRawMode();

        return input;
    }
    public String validateInput(String input, int minLength, int maxLength) {
        String blocked = "'\";:\\/|<>,.?*&%$#!@()[]{}=+~`^";

        return validateInput(input, blocked, minLength, maxLength);
    }

    public String validateInput(String input, String blocked, int minLength, int maxLength) {
        if (input == null) {
            return null;
        }
        input = input.trim();
        if (input.length() < minLength) {
            return String.format("input '%s' is too short (min %d)", input, minLength);
        }
        else if (input.length() > maxLength) {
            return String.format("input '%s' is too long (max %d)", input, maxLength);
        }

        for (char c : input.toCharArray()) {
            if (blocked.indexOf(c) >= 0) {
                return String.format("input '%s' can't contain '%c'", input, c);
            }
        }
        return null;
    }
}
