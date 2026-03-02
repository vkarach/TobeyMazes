package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;

import java.time.Duration;

public class GameMenu {
    private final Console console;
    private final TextRenderer textRenderer;

    private final int selectUIX = 30;

    public enum MenuOption {
        START("Start game"),
        PROFILE("Profile"),
        LEADERBOARD("Leader board"),
        ABOUT("About"),
        EXIT("Exit");

        private final String title;

        MenuOption(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public enum LevelOption {
        INTRODUCTION(Level.INTRODUCTION, null),
        IDK_FOR_NOW(Level.IDK_FOR_NOW, null),
        BACK(null, "Back");

        private final Level level;
        private final String title;

        LevelOption(Level level, String title) {
            this.level = level;
            this.title = title;
        }

        public Level getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return title != null ? title : level.toString();
        }
    }

    public GameMenu(Console console) {
        this.console = console;
        this.textRenderer = new TextRenderer(console);
    }

    public MenuOption launch() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/game_name.txt", 0, 0);

        MenuOption[] actions = new MenuOption[]{
                MenuOption.START,
                MenuOption.PROFILE,
                MenuOption.LEADERBOARD,
                MenuOption.ABOUT,
                MenuOption.EXIT,
        };

        MenuOption result = select(actions);

        return result == null ? MenuOption.EXIT : result;
    }

    public Level selectLevel() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/select_level.txt", 0, 0);

        LevelOption[] options = LevelOption.values();

        LevelOption selected = select(options);

        if (selected == null || selected == LevelOption.BACK) {
            return null;
        }

        return selected.getLevel();
    }

    public void aboutPage() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/about_title.txt", 0, 0);
        textRenderer.renderFromFile("uiTexts/about_text.txt", 0, 10);

        fakeChoose(15);
    }

    public String profilePage() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/login_to_continue.txt", 0, 0);

        String[] options = new String[] {
                "Login",
                "Back"
        };

        return select(options);
    }

    public void profilePage(User user) { // maybe rename or that even better???
        console.clear();

        textRenderer.renderFromFile("uiTexts/your_profile.txt", 0, 0);

        String name = String.format("Name: %s", user.getName());
        // todo: more information

        console.print(name, 20, 20);

        fakeChoose(25); // todo choose to logout return just chosen option handle in AuthService
    }

    public void winPage(long playedTime) {
        Duration duration = Duration.ofNanos(playedTime);

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).toSeconds();
        long millis = duration.minusMinutes(minutes)
                .minusSeconds(seconds)
                .toMillis();
        millis /= 10;

        console.clear();

        console.print(String.format("you win in %02d:%02d:%02d :)",  minutes, seconds, millis));

        fakeChoose(15);
    }

    private void fakeChoose(int y) {
        console.print("▶ Back",
                selectUIX, y,
                AttributedStyle.DEFAULT.background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK)
        );

        while (true) {
            InputType input = console.readAction();
            if (input == InputType.ENTER || input == InputType.QUIT) {
                break;
            }
        }
    }

    private <T> T select(T[] items) {
        int choose = 0;

        int longest = 0;
        for (T item: items) {
            longest = Math.max(longest, item.toString().length());
        }

        selectLoop:
        while (true) {
            InputType input = console.readAction();

            switch (input) {
                case DOWN  -> choose = (choose + 1) % items.length;
                case UP    -> choose = (choose - 1 + items.length) % items.length;
                case ENTER -> { return items[choose]; }
                case QUIT  -> { break selectLoop; }
            }

            int x = selectUIX;
            int y = 20;
            for (int i = 0; i < items.length; i++) {
                String str = String.format("%-" + longest + "s", items[i].toString());
                if (i == choose) {
                    console.print("▶ " + str,
                            x, y + i,
                            AttributedStyle.DEFAULT.inverse()
                    );
                }
                else {
                    console.print("  " + str, x, y + i);
                }
            }
        }
        return null;
    }
}
// >/▶/➤/▸/» settings or A_REVERSE A_BOLD