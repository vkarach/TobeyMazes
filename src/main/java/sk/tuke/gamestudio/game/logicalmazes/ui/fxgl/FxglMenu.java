package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.game.logicalmazes.ui.MenuOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.MenuView;
import sk.tuke.gamestudio.game.logicalmazes.ui.ProfileOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Profile("fxgl")
@Component
public class FxglMenu implements MenuView {
    private final MainMenuPage     mainMenuPage;
    private final AboutPage        aboutPage;
    private final ProfilePage      profilePage;
    private final LeaderboardPage  leaderboardPage;
    private final ReviewPage       reviewPage;
    private final LevelSelectPage  levelSelectPage;
    private final FxglLevelView    levelView;
    private final Selector         selector;
    private final FxglInput        gameInput;

    private Level   lastPlayedLevel;
    private boolean replayRequested = false;

    public FxglMenu(
            MainMenuPage    mainMenuPage,
            AboutPage       aboutPage,
            ProfilePage     profilePage,
            LeaderboardPage leaderboardPage,
            ReviewPage      reviewPage,
            LevelSelectPage levelSelectPage,
            FxglLevelView   levelView,
            Selector        selector,
            FxglInput       gameInput
    ) {
        this.mainMenuPage    = mainMenuPage;
        this.aboutPage       = aboutPage;
        this.profilePage     = profilePage;
        this.leaderboardPage = leaderboardPage;
        this.reviewPage      = reviewPage;
        this.levelSelectPage = levelSelectPage;
        this.levelView       = levelView;
        this.selector        = selector;
        this.gameInput       = gameInput;
    }

    @Override public MenuOption mainMenu() { return mainMenuPage.show(); }

    @Override
    public Level selectLevel(User currentUser) {
        if (replayRequested) {
            replayRequested = false;
            return lastPlayedLevel;
        }
        Level selected = levelSelectPage.show(currentUser);
        if (selected != null) {
            lastPlayedLevel = selected;
            levelView.setCurrentLevel(selected);
        }
        return selected;
    }

    @Override public void reviewPage(User currentUser)    { reviewPage.show(currentUser); }
    @Override public void aboutPage()                     { aboutPage.show(); }
    @Override public void leaderboardPage(User user)      { leaderboardPage.show(user); }
    @Override public ProfileOption guestProfilePage()     { return profilePage.showGuest(); }
    @Override public ProfileOption authorizedProfilePage(User u) { return profilePage.showAuthorized(u); }

    @Override
    public void winPage(long playedTimeMs, int points, boolean isTimeRecord, boolean isScoreRecord) {
        replayRequested = buildWinOverlay(playedTimeMs, points, isTimeRecord, isScoreRecord);
    }

    @Override
    public void exit() { Platform.runLater(() -> FXGL.getGameController().exit()); }

    // ─── Win overlay ─────────────────────────────────────────────────────────

    private boolean buildWinOverlay(long playedTimeMs, int points,
                                    boolean isTimeRecord, boolean isScoreRecord) {
        CompletableFuture<Boolean> choice = new CompletableFuture<>();
        CountDownLatch latch = new CountDownLatch(1);
        FxglUi.Modal[] modalHolder = new FxglUi.Modal[1];

        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            double W = FXGL.getAppWidth(), H = FXGL.getAppHeight();

            Group content = new Group();
            content.setTranslateX(W / 2.0);
            content.setTranslateY(H / 2.0);

            double iy = -110;

            Text title = FxglUi.createText("LEVEL CLEAR", 36, FxglUi.DEFAULT_TITLE_COLOR);
            title.setTranslateX(-title.getLayoutBounds().getWidth() / 2.0);
            title.setTranslateY(iy - title.getLayoutBounds().getMinY());
            content.getChildren().add(title);
            iy += title.getLayoutBounds().getHeight() + 32;

            double colL = -90, colR = 90;
            double lblH = FxglUi.createText("X", 8, Color.WHITE).getLayoutBounds().getHeight();
            double valH = FxglUi.createText("X", 20, Color.WHITE).getLayoutBounds().getHeight();
            double recH = FxglUi.createText("X", 7, Color.WHITE).getLayoutBounds().getHeight();
            double rowH = lblH + 6 + valH + 6 + recH + 4;

            addStatColToGroup(content, "TIME",  formatTime(playedTimeMs), isTimeRecord,  colL, iy, lblH);
            addStatColToGroup(content, "SCORE", String.valueOf(points),   isScoreRecord, colR, iy, lblH);
            iy += rowH + 16;

            Rectangle sep = FxglUi.createGradientSep(-160, iy, 320);
            content.getChildren().add(sep);
            iy += 2 + 22;

            Text replayBtn = FxglUi.createText("PLAY AGAIN",   FxglUi.NAV_BTN_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            Text levelsBtn = FxglUi.createText("SELECT LEVEL", FxglUi.NAV_BTN_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);

            replayBtn.setTranslateX(colL - replayBtn.getLayoutBounds().getWidth() / 2.0);
            replayBtn.setTranslateY(iy - replayBtn.getLayoutBounds().getMinY());
            levelsBtn.setTranslateX(colR - levelsBtn.getLayoutBounds().getWidth() / 2.0);
            levelsBtn.setTranslateY(iy - levelsBtn.getLayoutBounds().getMinY());

            replayBtn.setOnMouseClicked(e -> choice.complete(true));
            levelsBtn.setOnMouseClicked(e -> choice.complete(false));
            FxglUi.wireMenuButton(replayBtn, FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DEFAULT_ACTIVATION_COLOR);
            FxglUi.wireMenuButton(levelsBtn, FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DEFAULT_ACTIVATION_COLOR);

            content.getChildren().addAll(replayBtn, levelsBtn);

            modalHolder[0] = FxglUi.openModal(scene, 0.92, content);
            latch.countDown();
        });
        awaitLatch(latch);

        // Keyboard: ENTER → play again, Q → select levels, LEFT/RIGHT → toggle
        selector.ensureBindings();
        AtomicBoolean replayFocused = new AtomicBoolean(true);

        Thread keyThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                InputType in = gameInput.getInput();
                switch (in) {
                    case ENTER -> { choice.complete(replayFocused.get()); return; }
                    case QUIT  -> { choice.complete(false);               return; }
                    case LEFT, RIGHT -> {
                        boolean nowReplay = !replayFocused.getAndSet(!replayFocused.get());
                        Platform.runLater(() -> {
                            // highlight via color — buttons store their original color
                        });
                    }
                    default -> {}
                }
            }
        }, "win-key");
        keyThread.setDaemon(true);
        keyThread.start();

        boolean replay;
        try {
            replay = choice.get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            replay = false;
        }
        keyThread.interrupt();

        CountDownLatch closedLatch = new CountDownLatch(1);
        Platform.runLater(() -> modalHolder[0].close(closedLatch::countDown));
        awaitLatch(closedLatch);

        return replay;
    }

    private void addStatColToGroup(Group group, String labelStr, String valueStr, boolean isRecord,
                                    double cx, double iy, double lblH) {
        Text label = FxglUi.createText(labelStr, 8, FxglUi.DIM_WHITE);
        label.setTranslateX(cx - label.getLayoutBounds().getWidth() / 2.0);
        label.setTranslateY(iy - label.getLayoutBounds().getMinY());
        group.getChildren().add(label);
        iy += lblH + 6;

        Text value = FxglUi.createText(valueStr, 20, FxglUi.DEFAULT_TITLE_COLOR);
        double vh = value.getLayoutBounds().getHeight();
        value.setTranslateX(cx - value.getLayoutBounds().getWidth() / 2.0);
        value.setTranslateY(iy - value.getLayoutBounds().getMinY());
        group.getChildren().add(value);
        iy += vh + 6;

        if (isRecord) {
            Text rec = FxglUi.createText("\u2605 NEW RECORD", 7, FxglUi.DEFAULT_TITLE_COLOR);
            rec.setTranslateX(cx - rec.getLayoutBounds().getWidth() / 2.0);
            rec.setTranslateY(iy - rec.getLayoutBounds().getMinY());
            group.getChildren().add(rec);
        }
    }

    private static String formatTime(long ms) {
        if (ms < 0) ms = 0;
        long s  = ms / 1000;
        long cs = (ms % 1000) / 10;
        return String.format("%d:%02d", s, cs);
    }

    private void awaitLatch(CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
