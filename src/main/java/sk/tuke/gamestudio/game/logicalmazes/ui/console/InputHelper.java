package sk.tuke.gamestudio.game.logicalmazes.ui.console;

import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.ui.GameInput;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;

@Profile("console")
@Component
public class InputHelper {
    private final Console console;

    private final SoundUtil clickSound = new SoundUtil("sounds/enter.wav");
    private final GameInput gameInput;

    public InputHelper(Console console, GameInput gameInput) {
        this.console = console;
        this.gameInput = gameInput;
    }

    public String getUserInput(String prompt, int x, int y) {
        return getUserInput(prompt, x, y, 80);
    }

    public String getUserInput(String prompt, int x, int y, int clearLen) {
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

    public void waitForConfirm(String text, int x, int y) {
        console.print("▶ " + text, x, y,
                AttributedStyle.DEFAULT.background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK)
        );
        while (true) {
            InputType input = gameInput.getInput();
            if (input == InputType.ENTER || input == InputType.QUIT) {
                clickSound.play();
                return;
            }
        }
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
