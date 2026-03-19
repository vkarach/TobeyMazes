package sk.tuke.gamestudio.game.logicalmazes.core;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.console.AuthConsole;
import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.GameMenu;
import sk.tuke.gamestudio.service.ReviewService;
import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.impl.*;
import sk.tuke.gamestudio.service.AuthService;
import sk.tuke.gamestudio.service.impl.JDBC.*;

@Component
public class Game {
    private final Console console;
    private final LevelManager levelManager;
    private final GameMenu gameMenu;
    private final AuthService authService;
    private final AuthConsole authConsole;
    private final ReviewService reviewService;
    private User currentUser;

    public Game(
            Console console,
            GameMenu gameMenu,
            LevelManager levelManager,
            AuthService authService,
            AuthConsole authConsole
        ) {
        this.console = console;

        ReviewService reviewService = new ReviewServiceJDBC();  // todo: JPA
        new LevelServiceJDBC().syncLevelsFromEnum(Level.class); // todo: JPA

        this.gameMenu = gameMenu;
        this.levelManager = levelManager;
        this.reviewService = reviewService;
        this.authService = authService;
        this.authConsole = authConsole;

        this.currentUser = authService.getUserBySessionToken();

//        handleProfile();
//        levelManager.playLevel(Level.LEVEL3);
//        gameMenu.winPage( 2_500_000_000L, 369, true, true);
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
        console.setCursorPosition(0, 0);
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

            if (playedResult.levelState() == LevelState.SOLVED) {
                int score = levelManager.computePoints(
                        playedResult.playedTimeNs(),
                        playedResult.stepCount(),
                        level.getDifficulty()
                );

                boolean isTimeRecord = false;
                if (currentUser != null) {
                 isTimeRecord = levelManager.checkAndUpdateBestTime(
                            currentUser.getId(),
                            level.getId(),
                            (int) (playedResult.playedTimeNs() / 1_000_000)
                    );
                }
                boolean isScoreRecord = false;
                if (currentUser != null) {
                    isScoreRecord = levelManager.checkAndUpdateBestScore(
                            currentUser.getId(),
                            level.getId(),
                            score
                    );
                }
                gameMenu.winPage(playedResult.playedTimeNs(), score, isTimeRecord, isScoreRecord);
            }
        }
    }

    private void handleProfile() {
        if (currentUser == null) {
            handleGuestProfile();
        }

        if (currentUser != null) {
            handleAuthorizedProfile();
        }
    }

    private void handleGuestProfile() {
        GameMenu.ProfileOption selected;

        do {
            selected = gameMenu.guestProfilePage();
            if (selected == null || selected == GameMenu.ProfileOption.BACK) {
                return;
            }

            switch (selected) {
                case REGISTER -> currentUser = authConsole.register();
                case LOGIN -> currentUser = authConsole.login();
            }
        }
        while (currentUser == null);
    }

    private void handleAuthorizedProfile() {
        while (currentUser != null) {
            GameMenu.ProfileOption selected = gameMenu.authorizedProfilePage(currentUser);
            if (selected == null || selected == GameMenu.ProfileOption.BACK) {
                return;
            }

            switch (selected) {
                case LOGOUT -> {
                    authService.deleteSession();
                    currentUser = null;
                    handleGuestProfile();
                }
                case CHANGE_PASSWORD -> authConsole.changePassword(currentUser.getId());
            }
        }
    }
}
