package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.*;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;
import sk.tuke.gamestudio.service.BestResultService;
import sk.tuke.gamestudio.service.ReviewService;

import java.time.Duration;
import java.util.List;
import java.util.Random;

@Component
public class GameMenu {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final BestResultService bestResultService;
    private final InputHelper inputHelper;
    private final Notifier notifier;

    private final SoundUtil enterSound = new SoundUtil("sounds/enter.wav");
    private final SoundUtil navigationSound = new SoundUtil("sounds/navigate.wav");
    private final SoundUtil bell = new SoundUtil("sounds/bell.wav");
    private final SoundUtil confirmSound = new SoundUtil("sounds/confirm.wav");

    private final int selectUIX = 30;

    public GameMenu(
            Console console,
            BestResultService bestResultService,
            ConsoleRenderer consoleRenderer,
            InputHelper inputHelper,
            Notifier notifier
    ) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
        this.bestResultService = bestResultService;
        this.inputHelper = inputHelper;
        this.notifier = notifier;
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
        LEVEL_1(Level.LEVEL_1, null),
        LEVEL_2(Level.LEVEL_2, null),
        LEVEL_3(Level.LEVEL_3, null),
        LEVEL_4(Level.LEVEL_4, null),
        LEVEL_5(Level.LEVEL_5, null),
        LEVEL_6(Level.LEVEL_6, null),
        LEVEL_7(Level.LEVEL_7, null),
        LEVEL_8(Level.LEVEL_8, null),
        LEVEL_9(Level.LEVEL_9, null),
        LEVEL_10(Level.LEVEL_10, null),
        LEVEL_11(Level.LEVEL_11, null),
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

        String versionText = String.format("Beta v%s", Game.version);
        console.print(versionText, console.getWidth() - versionText.length(), console.getHeight() - 1);

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
        try {
            anim.join();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result == null ? MenuOption.EXIT : result;
    }

    private String coloredDifficulty(Level.Difficulty difficulty) {
        String escColor = switch (difficulty) {
            case EASY   -> "\033[32m";
            case NORMAL -> "\033[38;5;33m";
            case MEDIUM -> "\033[33m";
            case HARD   -> "\033[31m";
        };
        return escColor + difficulty + "\033[39m";
    }

    private String formatBestTime(Long bestTimeMs) {
        if (bestTimeMs == null) return "N/A";
        return String.format("%d:%02d", bestTimeMs / 1000, (bestTimeMs % 1000) / 10);
    }

    private LevelOption[] buildLevelOption(User currentUser) {
        List<BestLevelResult> bestResultsByUser = currentUser != null
                ? bestResultService.getBestResultsByUserId(currentUser.getId())
                : null;

        // logged-in visible width: 12 + 1 + 8 + 1 + 8 + 1 + 6 + 2 = 39
        final int titleWidth = 39;

        LevelOption[] options = LevelOption.values();
        for (LevelOption option : options) {
            if (option.getLevel() == null) {
                option.setTitle(String.format("%-" + (titleWidth - 1) + "s|", "Back"));
                continue;
            }

            String levelTitle = option.getLevel().getTitle();
            String dif = coloredDifficulty(option.getLevel().getDifficulty());
            int escLen = dif.length() - option.getLevel().getDifficulty().toString().length();

            if (currentUser != null) {
                String scoreStr = "---";
                String timeStr = "---";
                for (BestLevelResult br : bestResultsByUser) {
                    if (br.getId().getLevelId() == option.getLevel().getId()) {
                        scoreStr = "" + br.getBestScore();
                        timeStr = formatBestTime(br.getBestTimeMs());
                        break;
                    }
                }
                option.setTitle(String.format("%-12s %-" + (8 + escLen) + "s %-8s %-6s |", levelTitle, dif, scoreStr, timeStr));
            }
            else {
                option.setTitle(String.format("%-12s %-" + (8 + escLen) + "s %-8s %-6s |",
                        levelTitle, dif, "---", "---"));
            }
        }
        return options;
    }

    public Level selectLevel(User currentUser) {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/konek_tobey_big.txt", 75, 0);

        String[] files = new String[] {
                "uiTexts/frames/hardcore_frame.txt",
                "uiTexts/frames/ready_frame.txt",
                "uiTexts/frames/u_got_this_frame.txt",
                "uiTexts/frames/braining_frame.txt",
        };
        Random random = new Random();

        Thread anim = null;
        if (random.nextInt(20) == 0) { // 15-25 I think
            anim = consoleRenderer.renderAnimation("uiTexts/frames/brosky_anim.txt", 50, 127, 8);
        }
        else {
            String randomFile = files[random.nextInt(files.length)];
            consoleRenderer.renderFromFile(randomFile, 127, 8);
        }

        final String[] scrollStart = new String[] {
                "  _________________________________________  ",
                "=(__    ___    ____   __     ___    ___   _)=",
                "  |                                       |  "

        };
        final String[] scrollEnd = new String[] {
                "  |                                       |",
                "  |__  __   ___     __    __     ___   ___|",
                "=(_________________________________________)="
        };

        consoleRenderer.renderFromFile("uiTexts/select_level.txt");

        LevelOption[] options = buildLevelOption(currentUser);

        int y = 24;
        if (currentUser == null) {
            console.print("Login/Register to save your time/score", selectUIX - 2, y - scrollStart.length - 1);
        }

        for (int i = 0; i < options.length; i++) {
            console.print("|", selectUIX - 2, y + i);
        }
        consoleRenderer.renderStringList(scrollStart, selectUIX - 4, y - scrollStart.length);
        consoleRenderer.renderStringList(scrollEnd,   selectUIX - 4, y + options.length);
        console.print(String.format("%-12s %-8s %-8s %-6s", "Level", "Diff", "Score", "Time"),
                selectUIX, y - 1, AttributedStyle.DEFAULT.foreground(245));

        LevelOption selected = select(options, "", selectUIX, y);

        if (anim != null) {
            anim.interrupt();
        }
        if (selected == null || selected == LevelOption.BACK) {
            return null;
        }

        return selected.getLevel();
    }

    private void printRating(ReviewService reviewService, int x, int y) {
        float overallRating = reviewService.getOverallRating();
        String ratingText = String.format("Overall getRating: %s",
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

        Review review = reviewService.getReview(currentUser.getId());
        if (review != null) {
            String reviewText = String.format("%d★ %s", review.getRating(), !review.getComment().isEmpty() ? review.getComment() : "(without comment)");
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

        reviewService.addOrUpdateReview(new Review(currentUser.getId(), ratingValue, commentText));

        console.clearLine(selectUIX, y + 1);
        console.print(commentText, selectUIX, y + 1);

        printRating(reviewService, 65, y - 1);

         confirmSound.play();

        console.print("Thank you for your feedback!", selectUIX, y + 3);
        fakeChoose(selectUIX, y + 5);
    }

    public void aboutPage() {
        console.clear();

        consoleRenderer.renderFromFile("uiTexts/about_title.txt");

        int x = 10;
        consoleRenderer.renderFromFile("uiTexts/about_text.txt", x, 12, false, Game.version, Game.author);

        fakeChoose(x, 20);
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

        Integer bestScore = bestResultService.getBestOverallScore(user.getId());

        String horzBound = "+" + "-".repeat(25) + "+";
        String name = String.format("Name: %s", user.getName());
        String score = String.format("Overall score: %d", bestScore != null ? bestScore : 0);

        consoleRenderer.renderFromFile("uiTexts/konek_tobey_big.txt", 75, 0);
        consoleRenderer.renderFromFile("uiTexts/frames/hello_there_frame.txt", 127, 8);

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

    public void winPage(long playedTimeMs, int points, boolean isTimeRecord, boolean isScoreRecord) {
        Duration duration = Duration.ofMillis(playedTimeMs);

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
        consoleRenderer.renderFromFile("uiTexts/trophy.txt", 85, console.getHeight() - size.height());

        List<UserScore> topUserScores = bestResultService.getTopByScore();
        if (topUserScores.isEmpty()) {
            console.print("No one here yet :(", 30, 13);
            fakeChoose(selectUIX, 15);
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

    private void fakeChoose(int x, int y) {
        fakeChoose(x, y, "Back");
    }

    private void fakeChoose(int x, int y, String text) {
        inputHelper.waitForConfirm(text, x, y);
    }

    private <T> T select(T[] items) {
        return select(items, "▶ ", selectUIX, 20);
    }

    private <T> T select(T[] items, int x, int y) {
        return select(items, "▶ ", x, y);
    }

    private <T> T select(T[] items, String pointer, int x, int y) {
        int longest = getLongest(items);

        int choose = 0;

        selectLoop:
        while (true) {
            InputType input = console.readAction();

            switch (input) {
                case DOWN  -> {
                    navigationSound.play();
                    choose = (choose + 1) % items.length;
                }
                case UP    -> {
                    navigationSound.play();
                    choose = (choose - 1 + items.length) % items.length;
                }
                case ENTER -> {
                    enterSound.play();
                    return items[choose];
                }
                case QUIT  -> {
                    break selectLoop;
                }
            }

            for (int i = 0; i < items.length; i++) {
                String itemStr = items[i].toString();
                String stripped = itemStr.replaceAll("\033\\[[\\d;]*m", "");
                int escLen = itemStr.length() - stripped.length();
                String str = String.format("%-" + (longest + escLen) + "s", itemStr);
                if (i == choose) {
                    console.print(pointer + str,
                            x, y + i,
                            AttributedStyle.DEFAULT.inverse() // .blink()
                    );
                }
                else {
                    console.print(" ".repeat(pointer.length()) + str, x, y + i);
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

        boolean plusRating = true;
        int combo = 0;
        while (true) {
            InputType input = console.readAction();

            switch (input) {
                case RIGHT -> {
                    if (rating < 5) {
                        rating = rating + 1;
                        bell.play();
                    }
                }
                case LEFT  -> {
                    if (rating > 1) {
                        rating = rating - 1;
                        bell.play();
                    }
                }
                case ENTER -> {
                    enterSound.play();
                    return rating;
                }
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
            String stripped = item.toString().replaceAll("\033\\[[\\d;]*m", "");
            longest = Math.max(longest, stripped.length());
        }
        return longest;
    }
}
// >/▶/➤/▸/» settings or A_REVERSE A_BOLD