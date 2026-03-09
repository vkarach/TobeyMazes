package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.service.impl.BestResultServiceJDBC;
import sk.tuke.gamestudio.service.BestResultService;

import java.time.Duration;
import java.util.List;

public class GameMenu {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final BestResultService bestResultService;

    private final int selectUIX = 30;

    public GameMenu(Console console) {
        this.console = console;
        this.consoleRenderer = new ConsoleRenderer(console);
        this.bestResultService = new BestResultServiceJDBC();
    }

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
        INTRODUCTION(Level.INTRODUCTION, Level.INTRODUCTION.toString()),
        IDK_FOR_NOW(Level.IDK_FOR_NOW,   Level.INTRODUCTION.toString()),
        BACK(null, "Back");

        private final Level level;
        private String title;

        LevelOption(Level level, String title) {
            this.level = level;
            this.title = title;
        }

        public Level getLevel() {
            return level;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
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

    public MenuOption launch() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/game_name.txt");

//        Thread anim = consoleRenderer.renderAnimation("animations/anim_test.txt", 50, 50, 20);

//        AtomicInteger oldH = new AtomicInteger(console.getHeight());
//        Thread cleaner = new Thread(() -> {
//            while (true) {
//                if (console.getHeight() != oldH.get()) {
//                    oldH.set(console.getHeight());
//                    console.clear();
//                    consoleRenderer.renderFromFile("uiTexts/game_name.txt");
//                }
//            }
//        });
//        cleaner.setDaemon(true);
//        cleaner.start();

        MenuOption[] actions = new MenuOption[]{
                MenuOption.START,
                MenuOption.PROFILE,
                MenuOption.LEADERBOARD,
                MenuOption.ABOUT,
                MenuOption.EXIT,
        };

        MenuOption result = select(actions);

//        anim.interrupt();
//        cleaner.interrupt();
        return result == null ? MenuOption.EXIT : result;
    }

    private LevelOption[] buildLevelOption(User currentUser) {
        LevelOption[] options = LevelOption.values();
        if (currentUser == null) {
            return options;
        }

        for (LevelOption option : options) {
            if (option.getLevel() == null) continue;

            String str;
            Integer bestTimeMs = bestResultService.getBestTime(currentUser.getId(), option.getLevel().getId());
            if (bestTimeMs != null) {
                str = String.format("%d:%02d", bestTimeMs / 1000, (bestTimeMs % 1000) / 10);
            }
            else {
                str = "N/A";
            }

            option.setTitle(option.getLevel() + str);
        }
        return options;
    }

    public Level selectLevel(User currentUser) {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/select_level.txt");

        LevelOption[] options = buildLevelOption(currentUser);


        if (currentUser == null) {
            console.print("Login/Register to save your best time", selectUIX + 2, 20);
        }
        LevelOption selected = select(options, selectUIX, 22);

        if (selected == null || selected == LevelOption.BACK) {
            return null;
        }

        return selected.getLevel();
    }

    public void aboutPage() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/about_title.txt");
        consoleRenderer.renderFromFile("uiTexts/about_text.txt", 0, 10);

        fakeChoose();
    }

    public ProfileOption profilePage() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/login_or_register.txt");

        ProfileOption[] options = new ProfileOption[] {
            ProfileOption.REGISTER,
            ProfileOption.LOGIN,
            ProfileOption.BACK,
        };

        return select(options);
    }

    public ProfileOption profilePage(User user) {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/your_profile.txt");

        String name = String.format("Name: %s", user.getName());
        // todo: more information

        console.print(name, 20, 20);

        ProfileOption[] options = new ProfileOption[] {
                ProfileOption.LOGOUT,
                ProfileOption.BACK
        };

        return select(options, selectUIX, 25);
    }

    public void winPage(long playedTime, int points, boolean isTimeRecord, boolean isScoreRecord) {
        Duration duration = Duration.ofNanos(playedTime);

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).toSeconds();
        long millis = duration.minusMinutes(minutes)
                .minusSeconds(seconds)
                .toMillis();
        millis /= 10;

        console.clear();

        console.print(String.format("you win in %02d:%02d:%02d and got %d points :)\n",  minutes, seconds, millis, points));

        if (isTimeRecord) {
            console.print("Its a new time record record!!!", 0, 1);
        }
        if (isScoreRecord) {
            console.print("Its a new score record record!!!", 0, 2);
        }
//      Thread anim = consoleRenderer.renderAnimation("animations/dansing.txt", 35, 55, 0);

        fakeChoose(30, 30);

//        if (anim != null) anim.interrupt();
    }

    public void leaderboardPage(User user) {
        Integer curUserId = user != null ? user.getId() : null;

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

        consoleRenderer.renderFromFile("uiTexts/leaderboard.txt");

        ConsoleRenderer.RenderSize size = consoleRenderer.getRenderFromFileSize("uiTexts/trophy.txt");
//        consoleRenderer.renderFromFile("uiTexts/trophy.txt", 85, 10);
        consoleRenderer.renderFromFile("uiTexts/trophy.txt", 85, console.getHeight() - size.height);

        List<UserScore> topUserScores = bestResultService.getTopByScore();
        if (topUserScores.isEmpty()) {
            console.print("No one here yet :(", 30, 13);
            fakeChoose();
            return;
        }
        String bestUserName = topUserScores.getFirst().getUserName();


        int leftPadding = (24 - bestUserName.length()) / 2;

        console.print("The best of the best",
                98, console.getHeight() - 6
        );
        console.print(bestUserName,
                95 + leftPadding, console.getHeight() - 4,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        );

        int x = 40;
        int y = 15;

        consoleRenderer.renderStringList(scrollStart, x - 2, y);
        y+=scrollStart.length;

        AttributedStyle[] topColors = new AttributedStyle[] {
                AttributedStyle.DEFAULT.foreground(220), // gold
                AttributedStyle.DEFAULT.foreground(159), // silver
                AttributedStyle.DEFAULT.foreground(130), // bronze 130, 166
        };

        int idx = 0;
        for (UserScore score : topUserScores) {
            AttributedStringBuilder asb = new AttributedStringBuilder();

            asb.append("| ");

            if (idx < topColors.length) {
                asb.style(topColors[idx]);
            }

            asb.append(String.format("%02d: %-20s", idx + 1, score.getUserName()));

            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));

            asb.append(String.format(" %03d |", score.getTotalScore()));

            if (curUserId != null && score.getUserId() == curUserId) {
                asb.append("<-- its you!!");
            }

            console.print(asb, x, y);

            y++;
            idx++;
        }

        consoleRenderer.renderStringList(scrollEnd, x - 2, y);
        y += scrollEnd.length;

        fakeChoose(x, y + 2);
    }

//    public void leaderboardPage(User user) {
//        console.clear();
//
//        final String[] scrollStart = new String[] {
//            "  ________________________________  ",
//            "=(__    ___    ___   __    ___   _)=",
//            "  |                              |  "
//
//        };
//        final String[] scrollEnd = new String[] {
//            "  |                              |",
//            "  |__  __   ___     __    __  ___|",
//            "=(________________________________)="
//        };
//
//        consoleRenderer.renderFromFile("uiTexts/leaderboard.txt");
//
//        ConsoleRenderer.RenderSize size = consoleRenderer.getRenderFromFileSize("uiTexts/trophy.txt");
////        consoleRenderer.renderFromFile("uiTexts/trophy.txt", 85, 10);
//        consoleRenderer.renderFromFile("uiTexts/trophy.txt", 85, console.getHeight() - size.height);
//
//        ScoreServiceJDBC scoreService = new ScoreServiceJDBC();
//        console.print("loading..." , 30, 13);
//        List<Score> topScores = scoreService.getTopScores("logicalmaze");
//        if (topScores.isEmpty()) {
//            console.print("No one here yet :(", 30, 13);
//            fakeChoose();
//            return;
//        }
//
//        String bestPlayerName = topScores.getFirst().getPlayer();
//
//        int leftPadding = (24 - bestPlayerName.length()) / 2;
//
//        console.print("The best of the best", 98, 37);
//        console.print(bestPlayerName, 95 + leftPadding, 39, AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
//
//        int x = 40;
//        int y = 15;
//
//        consoleRenderer.renderStringList(scrollStart, x - 2, y);
//        y+=scrollStart.length;
//
//        AttributedStyle[] topColors = new AttributedStyle[] {
//                AttributedStyle.DEFAULT.foreground(220), // gold
//                AttributedStyle.DEFAULT.foreground(159), // silver
//                AttributedStyle.DEFAULT.foreground(130), // bronze 130, 166
//        };
//
//        int idx = 0;
//        for (Score score : topScores) {
//            AttributedStringBuilder asb = new AttributedStringBuilder();
//
//            asb.append("| ");
//
//            if (idx < topColors.length) {
//                asb.style(topColors[idx]);
//            }
//
//            asb.append(String.format("%02d: %-20s", idx + 1, score.getPlayer()));
//
//            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
//
//            asb.append(String.format(" %03d |", score.getPoints()));
//            console.print(asb, x, y);
//
//            y++;
//            idx++;
//        }
//
//        consoleRenderer.renderStringList(scrollEnd, x - 2, y);
//        y += scrollEnd.length;
//
//        fakeChoose(x, y + 2);
//    }

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
                } else {
                    console.print("  " + str, x, y + i);
                }
            }
        }
        return null;
    }
}
// >/▶/➤/▸/» settings or A_REVERSE A_BOLD