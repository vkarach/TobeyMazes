package sk.tuke.gamestudio.game.logicalmazes.ui.console;

import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.ui.GameInput;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;

@Component
public class Selector {
    private final Console console;
    private final GameInput gameInput;

    private final SoundUtil enterSound = new SoundUtil("sounds/enter.wav");
    private final SoundUtil navigationSound = new SoundUtil("sounds/navigate.wav");
    private final SoundUtil bell = new SoundUtil("sounds/bell.wav");

    public static final int DEFAULT_X = 30;
    public static final int DEFAULT_Y = 20;
    private static final String POINTER = "▶ ";

    public Selector(Console console, GameInput gameInput) {
        this.console = console;
        this.gameInput = gameInput;
    }

    public <T> T select(T[] items) {
        return select(items, POINTER, DEFAULT_X, DEFAULT_Y);
    }

    public <T> T select(T[] items, int x, int y) {
        return select(items, POINTER, x, y);
    }

    public <T> T select(T[] items, String pointer, int x, int y) {
        int longest = getLongest(items);
        int choose = 0;

        while (true) {
            InputType input = gameInput.getInput();

            switch (input) {
                case DOWN  -> { navigationSound.play(); choose = (choose + 1) % items.length; }
                case UP    -> { navigationSound.play(); choose = (choose - 1 + items.length) % items.length; }
                case ENTER -> { enterSound.play(); return items[choose]; }
                case QUIT  -> { return null; }
            }

            for (int i = 0; i < items.length; i++) {
                String itemStr = items[i].toString();
                String stripped = itemStr.replaceAll("\033\\[[\\d;]*m", "");
                int escLen = itemStr.length() - stripped.length();
                String str = String.format("%-" + (longest + escLen) + "s", itemStr);
                if (i == choose) {
                    console.print(pointer + str, x, y + i, AttributedStyle.DEFAULT.inverse());
                } else {
                    console.print(" ".repeat(pointer.length()) + str, x, y + i);
                }
            }
        }
    }

    public Integer selectRating(int x, int y) {
        int rating = 1;
        final String emptyStar = "☆";
        final String fullStar  = "★";
        String[] rateEmoji = {"", "😠", "😞", "😐", "🙂", "🤩"};

        while (true) {
            InputType input = gameInput.getInput();
            switch (input) {
                case RIGHT -> { if (rating < 5) { rating++; bell.play(); } }
                case LEFT  -> { if (rating > 1) { rating--; bell.play(); } }
                case ENTER -> { enterSound.play(); return rating; }
                case QUIT  -> { return null; }
            }

            for (int i = 0; i < 5; i++) {
                console.print(rateEmoji[rating], x + 11, y);
                console.print(i < rating ? fullStar : emptyStar, x + i * 2, y);
            }
        }
    }

    private <T> int getLongest(T[] items) {
        int longest = 0;
        for (T item : items) {
            String stripped = item.toString().replaceAll("\033\\[[\\d;]*m", "");
            longest = Math.max(longest, stripped.length());
        }
        return longest;
    }
}