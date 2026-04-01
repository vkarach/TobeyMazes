package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;

import java.util.concurrent.CountDownLatch;

@Profile("fxgl")
@Component
public class AboutPage {

    private final Selector selector;
    private final ParallaxBackground bg;

    private static final Color TITLE_COLOR  = Color.rgb(133, 76, 196);
    private static final Color BUTTON_COLOR = Color.rgb(145, 205, 255);
    private static final Color SELECT_COLOR = Color.rgb(203, 145, 255);

    public AboutPage(Selector selector,
                     @Qualifier("aboutBackground") ParallaxBackground bg) {
        this.selector = selector;
        this.bg = bg;
    }

    public void show() {
        Text backBtn = buildUI();
        selector.waitForConfirm(backBtn, SELECT_COLOR);
    }

    private Text buildUI() {
        Text[] backBtn = new Text[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            String[] aboutText = new String[] {
                    "TobeyMazes v" + Game.version,
                    "An improved version of terminal - based maze game",
                    "Built with Java and Fxgl.",
                    "Made by " + Game.author + " — this is my fourth game.",
                    "",
                    "Thanks for participating in beta testing :)"
            };

            Text text1 = FxglUi.createTitle("ABOUT", TITLE_COLOR);
            double y = FxglUi.DEFAULT_PAD + text1.getLayoutBounds().getHeight();
            text1.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text1, -50);
            y*=2;

            Text text2 = FxglUi.createTitle("PAGE", TITLE_COLOR);
            text2.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text2, +50);
            y*=1.3;

            for (int i = 0; i < aboutText.length; i++) {
                Text text = FxglUi.createText(aboutText[i], FxglUi.DEFAULT_TEXT_SIZE, Color.WHITE);
                text.setTranslateY(y + i * FxglUi.DEFAULT_PAD);
                FxglUi.addTextCenteredX(scene, text);
            }

            backBtn[0] = FxglUi.createText(
                    "Back",
                    FxglUi.BUTTON_SIZE,
                    BUTTON_COLOR
            );
            backBtn[0].setTranslateY(y + aboutText.length * FxglUi.DEFAULT_PAD + FxglUi.DEFAULT_PAD);
            FxglUi.addTextCenteredX(scene, backBtn[0]);

            latch.countDown();
        });
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return backBtn[0];
    }
}
