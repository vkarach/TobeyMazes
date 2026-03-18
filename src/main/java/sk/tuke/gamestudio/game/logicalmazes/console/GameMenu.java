package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.entity.*;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.service.BestResultService;
import sk.tuke.gamestudio.service.ReviewService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class GameMenu {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final BestResultService bestResultService;

    private final int selectUIX = 30;

    public GameMenu(Console console, BestResultService bestResultService) {
        this.console = console;
        this.consoleRenderer = new ConsoleRenderer(console);
        this.bestResultService = bestResultService;
    }

    public enum MenuOption {
        START("Start game"),
        PROFILE("Profile"),
        LEADERBOARD("Leader board"),
        RATE("Rate game"),
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
        LEVEL1(Level.LEVEL1,   Level.LEVEL1.toString()),
        LEVEL2(Level.LEVEL2,   Level.LEVEL2.toString()),
        LEVEL3(Level.LEVEL3,   Level.LEVEL3.toString()),
        LEVEL4(Level.LEVEL4,   Level.LEVEL4.toString()),
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
        CHANGE_PASSWORD("Change password"),
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

        consoleRenderer.renderFromFile("uiTexts/game_title.txt");
        Thread anim = new KonekTobeyAnimation(console, consoleRenderer).startKonekTobeyAnimation(80, 20);

        MenuOption[] actions = new MenuOption[]{
                MenuOption.START,
                MenuOption.PROFILE,
                MenuOption.LEADERBOARD,
                MenuOption.RATE,
                MenuOption.ABOUT,
                MenuOption.EXIT,
        };

        MenuOption result = select(actions);

        anim.interrupt();

        return result == null ? MenuOption.EXIT : result;
    }

    private LevelOption[] buildLevelOption(User currentUser) {
        LevelOption[] options = LevelOption.values();
        if (currentUser == null) {
            for (LevelOption option : options) {
                if (option.getLevel() == null) continue;
                option.setTitle(option.getLevel().toString());
            }
            return options;
        }

        Map<Integer, Integer> bestTimesByLevel = bestResultService.getBestTimesByUser(currentUser.id());

        for (LevelOption option : options) {
            if (option.getLevel() == null) continue;

            String str;
            Integer bestTimeMs = bestTimesByLevel.get(option.getLevel().getId());

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

    private void printRating(ReviewService reviewService, int x, int y) {
        float overallRating = reviewService.getOverallRating();
        String ratingText = String.format("Overall rating: %s",
                overallRating > 0 ? String.format("%.2f", overallRating) : "no one rated yet"
        );
        console.print(ratingText, x, y);
    }

    public void reviewPage(User currentUser, ReviewService reviewService) {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/rate_title.txt");
        consoleRenderer.renderFromFile("uiTexts/stars.txt", 120, 0);
        consoleRenderer.renderFromFile("uiTexts/stars.txt", 120, 15);

        int y = 21;

        printRating(reviewService, 65, y - 1);

        if (currentUser == null) {
            console.print("Login to rate the game", selectUIX, y);
            fakeChoose(selectUIX, 30);
            return;
        }

        Review review = reviewService.getReview(currentUser.id());
        if (review != null) {
            String reviewText = String.format("%d★ %s", review.rating(), !review.comment().isEmpty() ? review.comment() : "without comment");
            console.print("You already rated the game:", selectUIX, y);
            console.print(reviewText, selectUIX, y + 1);
            String selected = select(new String[] { "Edit", "Back" }, selectUIX, y + 3);
            if (selected == null || selected.equals("Back")) return;
        }
        console.print("Rate the game ←→", selectUIX, y - 1);
        for (int i = 0; i < 5; i++) {
            console.clearLine(selectUIX, y + i);
        }

        Integer ratingValue = selectRating(selectUIX, y);
        if (ratingValue == null) return;

        InputHelper inputHelper = new InputHelper(console);
        Notifier notifier = new Notifier(console);
        String commentText;
        while (true) {
            commentText = inputHelper.getUserInput("Comment (optional): ", selectUIX, y + 1);
            if (commentText == null) {
                commentText = "";
            }
            if (commentText.isEmpty()) {
                break;
            }
            String error = inputHelper.validateInput(commentText, null, 6, 100);
            if (error != null) {
                notifier.showError(error, selectUIX, y + 2);
                continue;
            }
            break;
        }

        reviewService.addOrUpdateReview(new Review(currentUser.id(), ratingValue, commentText));

        console.print(commentText, selectUIX, y + 1);

        printRating(reviewService, 65, y - 1);

        console.print("Thank you for your feedback!", selectUIX, y + 3); // update overall rating ?
        fakeChoose(selectUIX, y + 5);
    }

    public void aboutPage() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/about_title.txt");
        consoleRenderer.renderFromFile("uiTexts/about_text.txt", 0, 10);

        fakeChoose();
    }

    public ProfileOption guestProfilePage() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/login_or_register.txt");
        ConsoleRenderer.RenderSize size = consoleRenderer.getRenderFromFileSize("uiTexts/who_are_you.txt");
        consoleRenderer.renderFromFile("uiTexts/who_are_you.txt", console.getWidth() - size.width() - 20, console.getHeight() - size.height());

        ProfileOption[] options = new ProfileOption[] {
            ProfileOption.REGISTER,
            ProfileOption.LOGIN,
            ProfileOption.BACK,
        };

        return select(options);
    }

    public ProfileOption authorizedProfilePage(User user) {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/your_profile.txt");

        Integer bestScore = bestResultService.getBestOverallScore(user.id());

        String horzBound = "+" + "-".repeat(25) + "+";
        String name = String.format("Name: %s", user.name());
        String score = String.format("Your score: %d", bestScore);

        consoleRenderer.renderFromFile("uiTexts/konek_tobey_big.txt", 75, 0);
        consoleRenderer.renderFromFile("uiTexts/hello_there_cloude.txt", 127, 8);

        int x = 20, y = 20;
        console.print(horzBound, x, y++);
        console.print(String.format("| %-23s |", name),  x,  y++);
        console.print(String.format("| %-23s |", score), x, y++);
        console.print(horzBound, x, y);

        ProfileOption[] options = new ProfileOption[] {
                ProfileOption.LOGOUT,
                ProfileOption.CHANGE_PASSWORD,
                ProfileOption.BACK
        };

        return select(options, x, 25);
    }

    public void winPage(long playedTimeNs, int points, boolean isTimeRecord, boolean isScoreRecord) {
        Duration duration = Duration.ofNanos(playedTimeNs);

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).toSeconds();
        long millis = duration.minusMinutes(minutes)
                .minusSeconds(seconds)
                .toMillis();
        millis /= 10;

        console.clear();

        consoleRenderer.renderFromFile("uiTexts/level_complete.txt");
        int x = 10;
        int y = 20;

        consoleRenderer.renderFromFile("uiTexts/megamind.txt", 50, y);

        console.print("+---------------------------+", x, y++);
        console.print("|        MEGA   MIND        |", x, y++);
        console.print("+---------------------------+", x, y++);
        y++;
        AttributedStringBuilder sb = new AttributedStringBuilder();

        AttributedStyle numbersStyle = AttributedStyle.DEFAULT.foreground(141);
        AttributedStyle recordStyle = AttributedStyle.DEFAULT.foreground(220);

        sb.append("| Time: ");
        if (minutes > 0) {
            sb.style(numbersStyle).append(String.valueOf(minutes)).append(":");
        }
        sb.style(numbersStyle).append(String.format("%02d:%02d", seconds, millis));
        console.print(sb, x, y++);

        sb = new AttributedStringBuilder();
        sb.append("| Points: ");
        sb.style(numbersStyle).append(String.valueOf(points));
        console.print(sb, x, y++);
        y++;

        if (isScoreRecord) {
            sb = new AttributedStringBuilder();
            sb.append("| ");
            sb.style(recordStyle).append("NEW SCORE RECORD!");
            console.print(sb, x, y++);
        }

        if (isTimeRecord) {
            sb = new AttributedStringBuilder();
            sb.append("| ");
            sb.style(recordStyle).append("BEAT YOUR BEST TIME!");
            console.print(sb, x, y++);
        }
        y++;
        console.print("+---------------------------+", x, y++);

        fakeChoose(x, y);
    }

    public void leaderboardPage(User user) {
        Integer curUserId = user != null ? user.id() : null;

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
        consoleRenderer.renderFromFile("uiTexts/trophy.txt", 85, console.getHeight() - size.height());

        List<UserScore> topUserScores = bestResultService.getTopByScore();
        if (topUserScores.isEmpty()) {
            console.print("No one here yet :(", 30, 13);
            fakeChoose();
            return;
        }
        String bestUserName = topUserScores.getFirst().userName();


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
                AttributedStyle.DEFAULT.foreground(130), // bronze
        };

        int idx = 0;
        for (UserScore score : topUserScores) {
            AttributedStringBuilder sb = new AttributedStringBuilder();

            sb.append("| ");

            AttributedStyle style;
            if (idx < topColors.length) {
                style = topColors[idx];
            }
            else {
                style = AttributedStyle.DEFAULT;
            }

            if (curUserId != null && score.userId() == curUserId) {
                style = style.italic().bold();
            }

            sb.style(style);
            sb.append(String.format("%02d: %-16s", idx + 1, score.userName()));

            sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));

            sb.append(String.format(" %7d |", score.totalScore()));

            console.print(sb, x, y);

            y++;
            idx++;
        }

        consoleRenderer.renderStringList(scrollEnd, x - 2, y);
        y += scrollEnd.length;

        fakeChoose(x, y + 2);
    }

    private void fakeChoose() {
        fakeChoose(selectUIX, 15, "Back");
    }

    private void fakeChoose(int x, int y) {
        fakeChoose(x, y, "Back");
    }

    private void fakeChoose(int x, int y, String text) {
        console.print("▶ " + text,
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
        int longest = getLongest(items);

        int choose = 0;

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

    private Integer selectRating(int x, int y) {
        int rating = 1;

        final String emptyStar = "☆";
        final String fullStar  = "★";

        String[] rateEmoji = new String[] {
            "", "😠", "😞", "😐", "🙂", "🤩"
        };

        while (true) {
            InputType input = console.readAction();

            switch (input) {
                case RIGHT -> rating = rating < 5 ? rating + 1 : rating;
                case LEFT  -> rating = rating > 1 ? rating - 1 : rating;
                case ENTER -> { return rating; }
                case QUIT  -> { return null; }
            }

            for (int i = 0; i < 5; i++) {
                console.print(rateEmoji[rating], x + 11, y);
                if (i < rating) {
                    console.print(fullStar, x + i * 2, y);
                }
                else {
                    console.print(emptyStar, x + i * 2, y);
                }
            }
        }
    }

    private <T> int getLongest(T[] items) {
        int longest = 0;
        for (T item: items) {
            longest = Math.max(longest, item.toString().length());
        }
        return longest;
    }
}
// >/▶/➤/▸/» settings or A_REVERSE A_BOLD