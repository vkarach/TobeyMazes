package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.ReviewService;

import java.util.concurrent.CountDownLatch;

@Profile("fxgl")
@Component
public class ReviewPage {
    private final ReviewService reviewService;
    private final Selector selector;
    private final ParallaxBackground bg;

    public ReviewPage(
            ReviewService reviewService,
            Selector selector,
            @Qualifier("aboutBackground") ParallaxBackground bg
    ) {
        this.reviewService = reviewService;
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

            Text text1 = FxglUi.createTitle("REVIEW", FxglUi.DEFAULT_TITLE_COLOR);
            double y = FxglUi.DEFAULT_PAD + text1.getLayoutBounds().getHeight();
            text1.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text1);
            y *= 2;

            Text text2 = FxglUi.createTitle("PAGE", FxglUi.DEFAULT_TITLE_COLOR);
            text2.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text2);
            y *= 1.3;

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
