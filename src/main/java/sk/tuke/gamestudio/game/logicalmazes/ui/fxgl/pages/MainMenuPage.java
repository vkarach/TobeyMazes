package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.ui.MenuOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Profile("fxgl")
@Component
public class MainMenuPage {
    private static final MenuOption[] ACTIONS = {
        MenuOption.START,
        MenuOption.PROFILE,
        MenuOption.LEADERBOARD,
        MenuOption.RATE,
        MenuOption.ABOUT,
        MenuOption.EXIT,
    };

    private final Selector selector;
    private final ParallaxBackground bg;

    public MainMenuPage(Selector selector, @Qualifier("mainMenuBackground") ParallaxBackground bg) {
        this.selector = selector;
        this.bg = bg;
    }

    public MenuOption show() {
        List<Text> buttons = buildUI();
        return selector.select(ACTIONS, buttons, FxglUi.DEFAULT_ACTIVATION_COLOR);
    }

    private List<Text> buildUI() {
        List<Text> buttons = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            Text text1 = FxglUi.createTitle("TOBEY", FxglUi.DEFAULT_TITLE_COLOR);
            double y = FxglUi.DEFAULT_PAD + text1.getLayoutBounds().getHeight();
            text1.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text1, -50);
            y*=2;

            Text text2 = FxglUi.createTitle("MAZES", FxglUi.DEFAULT_TITLE_COLOR);
            text2.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text2, +50);
            y*=1.3;

            for (int i = 0; i < ACTIONS.length; i++) {
                Text btn = FxglUi.createText(
                    ACTIONS[i].toString(),
                    FxglUi.BUTTON_SIZE,
                    FxglUi.DEFAULT_BUTTON_COLOR
                );
                buttons.add(btn);
                btn.setTranslateY(y + i * FxglUi.DEFAULT_PAD);
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
