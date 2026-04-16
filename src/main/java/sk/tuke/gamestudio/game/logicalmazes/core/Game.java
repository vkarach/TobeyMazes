package sk.tuke.gamestudio.game.logicalmazes.core;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.ui.AuthView;
import sk.tuke.gamestudio.game.logicalmazes.ui.MenuOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.MenuView;
import sk.tuke.gamestudio.game.logicalmazes.ui.ProfileOption;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;
import sk.tuke.gamestudio.service.*;

@Profile({"console", "fxgl"})
@Component
public class Game {
    public static final String version = "0.13.76";
    public static final String author = "Valentyn";

    private final MenuView menuView;
    private final LevelManager levelManager;
    private final AuthService authService;
    private final AuthView authView;

    private final SoundUtil backgroundLoop = new SoundUtil("sounds/jazz_loop.wav", 0.1f);
    private final SoundUtil playLoop = new SoundUtil("sounds/energizing_music_loop.wav", 0.05f);
    private final SoundUtil winSound = new SoundUtil("sounds/level_win.wav", 0.1f);

    private User currentUser;

    public Game(
            MenuView menuView,
            LevelManager levelManager,
            AuthService authService,
            LevelService levelService,
            AuthView authView) {
        this.menuView = menuView;
        this.levelManager = levelManager;
        this.authService = authService;
        this.authView = authView;


        levelService.syncLevelsFromEnum(Level.class);

        this.currentUser = authService.getUserBySessionToken();
        backgroundLoop.loop();

        SoundUtil.setVolumeCoef(0);
    }

    public void launch() {
        mainLoop:
        while (true) {
            MenuOption menuOption = menuView.mainMenu();
            if (menuOption == null) menuOption = MenuOption.EXIT;
            switch (menuOption) {
                case START       -> handleStartAndPlayLevel();
                case PROFILE     -> handleProfile();
                case LEADERBOARD -> menuView.leaderboardPage(currentUser);
                case RATE        -> menuView.reviewPage(currentUser);
                case ABOUT       -> menuView.aboutPage();
                case EXIT        -> {
                    backgroundLoop.stop();
                    menuView.exit();
                    break mainLoop;
                }
            }
        }
    }

    private void handleStartAndPlayLevel() {
        while (true) {
            Level level = menuView.selectLevel(currentUser);
            if (level == null) {
                break;
            }
            backgroundLoop.reset();
            backgroundLoop.stop();

            playLoop.loop();
            LevelManager.LevelResult playedResult = levelManager.playLevel(level);
            playLoop.reset();
            playLoop.stop();

            backgroundLoop.loop();
            if (playedResult.levelState() == LevelState.SOLVED) {
                winSound.play();
                int score = levelManager.computePoints(
                        playedResult.playedTimeNs(),
                        playedResult.stepCount(),
                        level.getDifficulty()
                );

                boolean isTimeRecord = false;
                boolean isScoreRecord = false;
                long playedTimeMs = playedResult.playedTimeNs() / 1_000_000;

                if (currentUser != null) {
                     isTimeRecord = levelManager.checkAndUpdateBestTime(
                                currentUser.getId(),
                                level.getId(),
                                (int) playedTimeMs
                     );

                     isScoreRecord = levelManager.checkAndUpdateBestScore(
                            currentUser.getId(),
                            level.getId(),
                            score
                    );
                }
                menuView.winPage(
                        playedTimeMs,
                        score,
                        isTimeRecord,
                        isScoreRecord
                );
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
        ProfileOption selected;

        do {
            selected = menuView.guestProfilePage();
            if (selected == null || selected == ProfileOption.BACK) {
                return;
            }

            switch (selected) {
                case REGISTER -> currentUser = authView.register();
                case LOGIN -> currentUser = authView.login();
            }
        }
        while (currentUser == null);
    }

    private void handleAuthorizedProfile() {
        while (currentUser != null) {
            ProfileOption selected = menuView.authorizedProfilePage(currentUser);
            if (selected == null || selected == ProfileOption.BACK) {
                return;
            }

            switch (selected) {
                case LOGOUT -> {
                    authService.deleteSession();
                    currentUser = null;
                    handleGuestProfile();
                }
                case CHANGE_PASSWORD -> authView.changePassword(currentUser.getId());
            }
        }
    }
}
