package sk.tuke.gamestudio.game.logicalmazes.console;

public class InputHelper {
    private final Console console;

    public InputHelper(Console console) {
        this.console = console;
    }

    public String getUserInput(String prompt, int x, int y) {
        return getUserInput(prompt, x, y, 80);
    }

    public String getUserInput(String prompt, int x, int y, int clearLen) {
//        console.print(" ".repeat(50), x + prompt.length(), y);
        console.setCursorPosition(x, y);

        console.exitRawMode();

        console.clearLine(clearLen, x, y);
        console.setCursorPosition(x, y);

        String input = console.readLine(prompt);
        if (input == null) {
            console.enterRawMode();
            return null;
        }

        console.setCursorPosition(x, y);

        console.enterRawMode();

        return input.trim();
    }

    public String validateInput(String input, int minLength, int maxLength) {
        String regex = "^[A-Za-z0-9_-]+$";

        return validateInput(input, regex, minLength, maxLength);
    }

    public String validateInput(String input, String regex, int minLength, int maxLength) {
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

        if (regex != null && !input.matches(regex)) {
            return String.format("input '%s' is invalid", input);
        }
        return null;
    }
}
