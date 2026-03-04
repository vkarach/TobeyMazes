package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.service.ScoreServiceJDBC;

import java.time.Duration;
import java.util.List;

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

    public enum ProfileOption {
        REGISTER("Register"),
        LOGIN("Login"),
        LOGOUT("Logout"),
        BACK("Back");

        private final String title;

        ProfileOption(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public GameMenu(Console console) {
        this.console = console;
        this.textRenderer = new TextRenderer(console);
    }

    public MenuOption launch() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/game_name.txt");

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

        textRenderer.renderFromFile("uiTexts/select_level.txt");

        LevelOption[] options = LevelOption.values();

        LevelOption selected = select(options);

        if (selected == null || selected == LevelOption.BACK) {
            return null;
        }

        return selected.getLevel();
    }

    public void aboutPage() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/about_title.txt");
        textRenderer.renderFromFile("uiTexts/about_text.txt", 0, 10);

        fakeChoose();
    }

    public ProfileOption profilePage() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/login_or_register.txt");

        ProfileOption[] options = new ProfileOption[] {
            ProfileOption.REGISTER,
            ProfileOption.LOGIN,
            ProfileOption.BACK,
        };

        return select(options);
    }

    public ProfileOption profilePage(User user) {
        console.clear();

        textRenderer.renderFromFile("uiTexts/your_profile.txt");

        String name = String.format("Name: %s", user.getName());
        // todo: more information

        console.print(name, 20, 20);

        ProfileOption[] options = new ProfileOption[] {
                ProfileOption.LOGOUT,
                ProfileOption.BACK
        };

        return select(options, selectUIX, 25);
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

        fakeChoose();
    }

    public void leaderboardPage(User user) {
        console.clear();

        final String[] scrollStart = new String[] {
            "  ________________________________  ",
            "=(__    ___    ___   __    ___   _)=",
            "  |                              |  "

        };
        final String[] scrollEnd = new String[] {
            "  |                              |",
            "  |__  __   ___     __    __  ___|",
            "=(________________________________)="
        };

        textRenderer.renderFromFile("uiTexts/leaderboard.txt");
        textRenderer.renderFromFile("uiTexts/trophy.txt", 85, 10);

        ScoreServiceJDBC scoreService = new ScoreServiceJDBC();
        List<Score> topScores = scoreService.getTopScores("logicalmaze");

        String bestPlayerName = topScores.getFirst().getPlayer();

        int leftPadding = (24 - bestPlayerName.length()) / 2;

        console.print("The best of the best", 98, 37);
        console.print(bestPlayerName, 95 + leftPadding, 39, AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));

        int x = 40;
        int y = 15;

        textRenderer.renderStringList(scrollStart, x - 2, y);
        y+=scrollStart.length;

        AttributedStyle[] topColors = new AttributedStyle[] {
                AttributedStyle.DEFAULT.foreground(220), // gold
                AttributedStyle.DEFAULT.foreground(159), // silver
                AttributedStyle.DEFAULT.foreground(130), // bronze 130, 166
        };

        int idx = 0;
        for (Score score : topScores) {
            AttributedStringBuilder asb = new AttributedStringBuilder();

            asb.append("| ");

            if (idx < topColors.length) {
                asb.style(topColors[idx]);
            }

            asb.append(String.format("%02d: %-20s", idx + 1, score.getPlayer()));

            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));

            asb.append(String.format(" %03d |", score.getPoints()));
            console.print(asb, x, y);

            y++;
            idx++;
        }

        textRenderer.renderStringList(scrollEnd, x - 2, y);
        y += scrollEnd.length;

//        int j = 0;
//        int q = 0;
//        for (int i = 0; i < 256; i++) {
//            if (q % 40 == 0) {
//                j += 10;
//                q = 0;
//            }
//            q++;
//            console.print("color " + i, j, q, AttributedStyle.DEFAULT.foreground(i));
//        }

        fakeChoose(x, y + 2);
    }

    private void fakeChoose() {
        fakeChoose(selectUIX, 15);
    }

    private void fakeChoose(int x, int y) {
        console.print("▶ Back",
                x, y,
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
        return select(items, selectUIX, 20);
    }

    private <T> T select(T[] items, int x, int y) {
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

            for (int i = 0; i < items.length; i++) {
                String str = String.format("%-" + longest + "s", items[i].toString());
                if (i == choose) {
                    console.print("▶ " + str,
                            x, y + i,
                            AttributedStyle.DEFAULT.inverse() // .blink()
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