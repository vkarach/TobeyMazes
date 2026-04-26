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

    public MainMenuPage(
            Selector selector,
            @Qualifier("mainMenuBackground") ParallaxBackground bg
    ) {
        this.selector = selector;
        this.bg = bg;
    }

    public MenuOption show() {
        Text[] indicatorHolder = new Text[1];
        List<Text> buttons = buildUI(indicatorHolder);
        return selector.select(ACTIONS, buttons, FxglUi.DEFAULT_ACTIVATION_COLOR, indicatorHolder[0]);
    }

    private List<Text> buildUI(Text[] indicatorHolder) {
        List<Text> buttons = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double appW = FXGL.getAppWidth();
            double appH = FXGL.getAppHeight();

            // Create nodes to measure heights before positioning
            Text t1 = FxglUi.createTitle("TOBEY", FxglUi.DEFAULT_TITLE_COLOR);
            Text t2 = FxglUi.createTitle("MAZES", FxglUi.DEFAULT_TITLE_COLOR);

            List<Text> btns = new ArrayList<>();
            for (MenuOption action : ACTIONS) {
                Color c = action == MenuOption.EXIT
                        ? Color.rgb(255, 107, 107, 0.7)
                        : FxglUi.DEFAULT_BUTTON_COLOR;
                btns.add(FxglUi.createText(action.toString(), FxglUi.BUTTON_SIZE, c));
            }

            double h1    = t1.getLayoutBounds().getHeight();
            double h2    = t2.getLayoutBounds().getHeight();
            double btnH  = btns.get(0).getLayoutBounds().getHeight();
            int    n     = ACTIONS.length;
            double blockH = h1 + 8 + h2 + 52 + n * btnH + (n - 1) * 15.0;

            // Center block matching CSS margin-top:-200 → center at appH/2 - 100
            double blockTop = (appH - blockH) / 2.0 - 100;

            // Title 1 — visual top at blockTop, offset -8px
            double y = blockTop;
            t1.setTranslateY(y - t1.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t1, -8);
            y += h1 + 8;

            // Title 2 — offset +8px
            t2.setTranslateY(y - t2.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t2, 8);
            y += h2 + 52;

            // Buttons — 15px gap between each
            for (int i = 0; i < n; i++) {
                Text btn = btns.get(i);
                double btnTop = y + i * (btnH + 15);
                btn.setTranslateY(btnTop - btn.getLayoutBounds().getMinY());
                btn.setCursor(javafx.scene.Cursor.HAND);
                FxglUi.addTextCenteredX(scene, btn);
                buttons.add(btn);
            }

            // ">" indicator — Selector animates its position when selection changes
            Text indicator = FxglUi.createText(">", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_TITLE_COLOR);
            scene.addUINode(indicator);
            indicator.setTranslateX(buttons.get(0).getTranslateX() - 28);
            indicator.setTranslateY(buttons.get(0).getTranslateY());
            indicatorHolder[0] = indicator;

            // Version tag — bottom right, matching CSS .version-tag
            Text vTag = FxglUi.createText(Game.versionLabel + Game.version, 11,
                    Color.rgb(245, 197, 24, 0.7));
            vTag.setTranslateX(appW - vTag.getLayoutBounds().getWidth() - 10);
            vTag.setTranslateY(appH - 6 - vTag.getLayoutBounds().getMaxY());
            scene.addUINode(vTag);

            latch.countDown();
        });
        await(latch);
        return buttons;
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
