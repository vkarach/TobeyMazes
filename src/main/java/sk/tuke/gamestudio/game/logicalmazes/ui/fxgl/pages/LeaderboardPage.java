package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.BestResultService;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Profile("fxgl")
@Component
public class LeaderboardPage {
    private final BestResultService bestResultService;
    private final Selector selector;
    private final ParallaxBackground bg;

    public LeaderboardPage(
           BestResultService bestResultService,
           Selector selector,
           @Qualifier("aboutBackground") ParallaxBackground bg
    ) {
        this.bestResultService = bestResultService;
        this.selector = selector;
        this.bg = bg;
    }

    public void show() {
        Text backBtn = buildUI(bg);
        selector.waitForConfirm(backBtn, FxglUi.DEFAULT_ACTIVATION_COLOR);
    }

        private Text buildUI(ParallaxBackground bg) {
            Text[] backBtn = new Text[1];
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                GameScene scene = FXGL.getGameScene();
                FxglUi.clearContentNodes(scene, bg.getAllNodes());
                bg.start(scene);

                Text text1 = FxglUi.createTitle("LEADERBOARD", FxglUi.DEFAULT_TITLE_COLOR);
                double y = FxglUi.DEFAULT_PAD + text1.getLayoutBounds().getHeight();
                text1.setTranslateY(y);
                FxglUi.addTextCenteredX(scene, text1);
                y*=2;

                List<UserScore> topUserScores = bestResultService.getTopByScore();

                for (UserScore userScore : topUserScores) {
                    y += FxglUi.DEFAULT_PAD;

                    String textStr = userScore.userName() + " - " + userScore.totalScore();
                    Text text = FxglUi.createText(textStr);
                    text.setTranslateY(y);
                    FxglUi.addTextCenteredX(scene, text);
                }
                y += FxglUi.DEFAULT_PAD;


                backBtn[0] = FxglUi.createText(
                        "Back",
                        FxglUi.BUTTON_SIZE,
                        FxglUi.DEFAULT_BUTTON_COLOR
                );
                backBtn[0].setTranslateY(y);
                FxglUi.addTextCenteredX(scene, backBtn[0]);

                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return backBtn[0];
        }
}
