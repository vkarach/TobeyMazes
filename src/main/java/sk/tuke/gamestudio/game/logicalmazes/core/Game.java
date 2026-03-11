package sk.tuke.gamestudio.game.logicalmazes.core;

import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.console.AuthConsole;
import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.GameMenu;
import sk.tuke.gamestudio.service.ReviewService;
import sk.tuke.gamestudio.service.impl.BestResultServiceJDBC;
import sk.tuke.gamestudio.service.impl.LevelServiceJDBC;
import sk.tuke.gamestudio.service.impl.ReviewServiceJDBC;
import sk.tuke.gamestudio.service.impl.UserServiceJDBC;

public class Game {
    private final Console console;
    private final LevelManager levelManager;
    private final GameMenu gameMenu;
    private final AuthService authService;
    private final AuthConsole authConsole;
    private final ReviewService reviewService;

    private User currentUser;

    public Game(Console console) {
        this.console = console;
        this.levelManager = new LevelManager(console);
        this.gameMenu = new GameMenu(console, new BestResultServiceJDBC());
        this.authService = new AuthService(new UserServiceJDBC());
        this.authConsole = new AuthConsole(console, authService);
        this.currentUser = authService.loadUserSession();
        this.reviewService = new ReviewServiceJDBC();
        new LevelServiceJDBC().syncLevelsFromEnum(Level.class);
//        Thread scaleThread = new Thread(() -> console.warnIfTerminalTooSmall(43));
//        scaleThread.setDaemon(true);
//        scaleThread.start();
//        gameMenu.reviewPage(currentUser, reviewService);
//        gameMenu.leaderboardPage(currentUser);
    }

    public void launch() {
        mainLoop:
        while (true) {
            GameMenu.MenuOption menuOption = gameMenu.launch();
            switch (menuOption) {
                case START       -> handleStartAndPlayLevel();
                case PROFILE     -> handleProfile();
                case LEADERBOARD -> gameMenu.leaderboardPage(currentUser);
                case RATE        -> gameMenu.reviewPage(currentUser, reviewService);
                case ABOUT       -> gameMenu.aboutPage();
                case EXIT        -> { exit(); break mainLoop; }
            }
        }
    }

    public void exit() {
        console.clear();
        console.print("exiting...\n");
        console.close();
    }

    private void handleStartAndPlayLevel() {
        while (true) {
            Level level = gameMenu.selectLevel(currentUser);
            if (level == null) {
                break;
            }

            LevelManager.LevelResult playedResult = levelManager.playLevel(level);

            if (playedResult.gameState == GameState.SOLVED) {
                int points = levelManager.computePoints(
                        playedResult.playedTimeNs,
                        playedResult.stepCount,
                        level.getDifficulty()
                );

                boolean isTimeRecord = false;
                if (currentUser != null) {
                 isTimeRecord = levelManager.checkAndUpdateBestTime( // it's only time, need points?
                            currentUser.getId(),
                            level.getId(),
                            (int) (playedResult.playedTimeNs / 1_000_000)
                    );
                }
                boolean isScoreRecord = false;
                if (currentUser != null) {
                    isScoreRecord = levelManager.checkAndUpdateBestScore(
                            currentUser.getId(),
                            level.getId(),
                            points
                    );
                }
                gameMenu.winPage(playedResult.playedTimeNs, points, isTimeRecord, isScoreRecord);
            }
        }
    }

    private void handleProfile() {
        if (currentUser == null) {
            GameMenu.ProfileOption selected;
            do {
                selected = gameMenu.profilePage();
                if (selected == null) return;
                switch (selected) {
                    case REGISTER -> currentUser = authConsole.register();
                    case LOGIN -> currentUser = authConsole.login();
                }
            }
            while (currentUser == null && selected != GameMenu.ProfileOption.BACK);
        }
        if (currentUser != null) {
            GameMenu.ProfileOption choose = gameMenu.profilePage(currentUser);
            if (choose == GameMenu.ProfileOption.LOGOUT) {
                authService.deleteSession();
                currentUser = null;
            }
        }
    }
}
