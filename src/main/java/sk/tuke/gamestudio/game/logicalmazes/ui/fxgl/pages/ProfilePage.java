package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.ui.ProfileOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Profile("fxgl")
@Component
public class ProfilePage {
    private final Selector selector;
    private final BestResultService bestResultService;
    private final ParallaxBackground bg;

    private static final ProfileOption[] AUTHORIZED_ACTIONS = {
            ProfileOption.LOGOUT, ProfileOption.CHANGE_PASSWORD, ProfileOption.BACK
    };
    private static final ProfileOption[] GUEST_ACTIONS = {
            ProfileOption.REGISTER, ProfileOption.LOGIN, ProfileOption.BACK
    };

    public ProfilePage(Selector selector, BestResultService bestResultService,
                       @Qualifier("profileBackground") ParallaxBackground bg) {
        this.selector = selector;
        this.bestResultService = bestResultService;
        this.bg = bg;
    }

    public ProfileOption showGuest() {
        List<Text> buttons = buildGuestUI();
        return selector.select(GUEST_ACTIONS, buttons, FxglUi.DEFAULT_ACTIVATION_COLOR);
    }

    public ProfileOption showAuthorized(User user) {
        List<Text> buttons = buildAuthorizedUI(user);
        return selector.select(AUTHORIZED_ACTIONS, buttons, FxglUi.DEFAULT_ACTIVATION_COLOR);
    }

    private List<Text> buildAuthorizedUI(User user) {
        List<Text> buttons = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            Text text1 = FxglUi.createTitle("YOUR", FxglUi.DEFAULT_TITLE_COLOR);
            double y = FxglUi.DEFAULT_PAD + text1.getLayoutBounds().getHeight();
            text1.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text1, -50);
            y*=2;

            Text text2 = FxglUi.createTitle("PROFILE", FxglUi.DEFAULT_TITLE_COLOR);
            text2.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text2, +50);
            y*=1.3;

            Text userName = FxglUi.createText(user.getName(), FxglUi.DEFAULT_TEXT_SIZE, Color.WHITE);
            userName.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, userName);
            y+= FxglUi.DEFAULT_PAD;

            Integer overallScore = bestResultService.getBestOverallScore(user.getId());
            String overallScoreStr =
                    "Overall score: " + (overallScore != null ? overallScore : 0);
            Text overallScoreText = FxglUi.createText(overallScoreStr, FxglUi.DEFAULT_TEXT_SIZE, Color.WHITE);
            overallScoreText.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, overallScoreText);

            y+= FxglUi.DEFAULT_PAD * 2;

            for (int i = 0; i < AUTHORIZED_ACTIONS.length; i++) {
                Text btn = FxglUi.createText(
                        AUTHORIZED_ACTIONS[i].toString(),
                        FxglUi.BUTTON_SIZE,
                        FxglUi.DEFAULT_BUTTON_COLOR
                );
                btn.setTranslateY(y + i * FxglUi.DEFAULT_PAD);
                buttons.add(btn);
                FxglUi.addTextCenteredX(scene, btn);
            }

            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return buttons;
    }

    private List<Text> buildGuestUI() {
        List<Text> buttons = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            Text text1 = FxglUi.createTitle("LOGIN", FxglUi.DEFAULT_TITLE_COLOR);
            double y = FxglUi.DEFAULT_PAD + text1.getLayoutBounds().getHeight();
            text1.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text1, -50);
            y*=2;

            Text text2 = FxglUi.createTitle("REGISTER", FxglUi.DEFAULT_TITLE_COLOR);
            text2.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text2, +50);
            y*=1.3;

            for (int i = 0; i < GUEST_ACTIONS.length; i++) {
                Text btn = FxglUi.createText(
                        GUEST_ACTIONS[i].toString(),
                        FxglUi.BUTTON_SIZE,
                        FxglUi.DEFAULT_BUTTON_COLOR
                );
                btn.setTranslateY(y + i * FxglUi.DEFAULT_PAD);
                buttons.add(btn);
                FxglUi.addTextCenteredX(scene, btn);
            }

            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return buttons;
    }
}
