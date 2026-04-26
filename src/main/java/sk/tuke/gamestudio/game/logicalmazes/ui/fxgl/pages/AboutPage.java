package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
    private static final double CARD_W   = 520;
    private static final double CARD_PAD = 28;

    private final Selector selector;
    private final ParallaxBackground bg;

    public AboutPage(
            Selector selector,
            @Qualifier("aboutBackground") ParallaxBackground bg
    ) {
        this.selector = selector;
        this.bg = bg;
    }

    public void show() {
        Text backBtn = buildUI();
        selector.waitForConfirm(backBtn, FxglUi.DEFAULT_ACTIVATION_COLOR);
    }

    private Text buildUI() {
        Text[] backBtn = new Text[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double appW = FXGL.getAppWidth();
            double appH = FXGL.getAppHeight();
            double cardX = (appW - CARD_W) / 2.0;

            // --- Measure card content height ---
            Text nameText    = FxglUi.createText("Tobey Mazes", 20, FxglUi.DEFAULT_TITLE_COLOR);
            Text verText     = FxglUi.createText("v" + Game.version, 11, Color.rgb(145, 205, 255, 0.9));
            Text howLabel    = FxglUi.createText("HOW TO PLAY", 12, FxglUi.DEFAULT_TITLE_COLOR);
            String[] items   = {
                "> Move with Arrows or WASD",
                "> You slide until you hit a wall",
                "> Collect all flowers to clear level",
                "> Fewer steps & faster = higher score"
            };
            Text[] itemTexts = new Text[items.length];
            for (int i = 0; i < items.length; i++)
                itemTexts[i] = FxglUi.createText(items[i], 9, Color.rgb(255, 255, 255, 0.85));

            Text madeByLabel = FxglUi.createText("Made by", 10, Color.rgb(255, 255, 255, 0.5));
            Text authorText  = FxglUi.createText(Game.author, 13, FxglUi.DEFAULT_TITLE_COLOR);
            Text thanksText  = FxglUi.createText("Thanks for playing!", 10, Color.rgb(145, 205, 255, 0.95));
            Text copyrText   = FxglUi.createText("\u00A9 2025-2026 Valentyn Karachevtsev", 6,
                    Color.rgb(255, 255, 255, 0.15));

            double sep = 2, gap = 10;
            double cardContentH = nameText.getLayoutBounds().getHeight() + 6
                    + verText.getLayoutBounds().getHeight() + gap
                    + sep + gap                                          // grad sep 1
                    + howLabel.getLayoutBounds().getHeight() + 8;
            for (Text t : itemTexts) cardContentH += t.getLayoutBounds().getHeight() + 5;
            cardContentH += gap + sep + gap                             // grad sep 2
                    + Math.max(madeByLabel.getLayoutBounds().getHeight(),
                               authorText.getLayoutBounds().getHeight()) + 10
                    + thanksText.getLayoutBounds().getHeight() + 20
                    + copyrText.getLayoutBounds().getHeight();
            double cardH = CARD_PAD * 2 + cardContentH;

            // --- Compute overall block top (margin-top ≈ -55) ---
            Text t1   = FxglUi.createSubTitle("ABOUT", FxglUi.DEFAULT_TITLE_COLOR);
            Text t2   = FxglUi.createSubTitle("PAGE",  FxglUi.DEFAULT_TITLE_COLOR);
            Text back = FxglUi.createText("BACK", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            double h1 = t1.getLayoutBounds().getHeight();
            double h2 = t2.getLayoutBounds().getHeight();
            double bH = back.getLayoutBounds().getHeight();
            double blockH = h1 + 8 + h2 + 36 + cardH + 24 + bH;
            double blockTop = (appH - blockH) / 2.0 - 27.5; // menuOffset=55 → 55/2

            // --- Draw titles ---
            double y = blockTop;
            t1.setTranslateY(y - t1.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t1, -8);
            y += h1 + 8;

            t2.setTranslateY(y - t2.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t2, 8);
            y += h2 + 36;

            // --- Draw card panel (behind content) ---
            Rectangle card = FxglUi.createCardPanel(cardX, y, CARD_W, cardH);
            scene.addUINode(card);

            double iy = y + CARD_PAD; // inner content top

            // Game name
            nameText.setTranslateY(iy - nameText.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, nameText);
            iy += nameText.getLayoutBounds().getHeight() + 6;

            // Version
            verText.setTranslateY(iy - verText.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, verText);
            iy += verText.getLayoutBounds().getHeight() + gap;

            // Separator 1
            scene.addUINode(FxglUi.createGradientSep(cardX + CARD_W * 0.2, iy, CARD_W * 0.6));
            iy += sep + gap;

            // HOW TO PLAY
            howLabel.setTranslateY(iy - howLabel.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, howLabel);
            iy += howLabel.getLayoutBounds().getHeight() + 8;

            // List items: left-align all items under the widest one, and
            // horizontally centre that block within the card so the "> " hints
            // sit at a consistent, legible indent rather than hugging the edge.
            double listW = 0;
            for (Text t : itemTexts) listW = Math.max(listW, t.getLayoutBounds().getWidth());
            double listX = cardX + (CARD_W - listW) / 2.0;
            for (Text t : itemTexts) {
                t.setTranslateY(iy - t.getLayoutBounds().getMinY());
                t.setTranslateX(listX);
                scene.addUINode(t);
                iy += t.getLayoutBounds().getHeight() + 5;
            }

            // Separator 2
            iy += gap;
            scene.addUINode(FxglUi.createGradientSep(cardX + CARD_W * 0.2, iy, CARD_W * 0.6));
            iy += sep + gap;

            // Made by + author (inline, centered)
            double rowW = madeByLabel.getLayoutBounds().getWidth()
                    + 12 + authorText.getLayoutBounds().getWidth();
            double rowX = (appW - rowW) / 2.0;
            double rowMaxH = Math.max(madeByLabel.getLayoutBounds().getHeight(),
                                      authorText.getLayoutBounds().getHeight());
            madeByLabel.setTranslateX(rowX);
            madeByLabel.setTranslateY(iy - madeByLabel.getLayoutBounds().getMinY());
            scene.addUINode(madeByLabel);
            authorText.setTranslateX(rowX + madeByLabel.getLayoutBounds().getWidth() + 12);
            authorText.setTranslateY(iy - authorText.getLayoutBounds().getMinY());
            scene.addUINode(authorText);
            iy += rowMaxH + 10;

            // Thanks for playing
            thanksText.setTranslateY(iy - thanksText.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, thanksText);
            iy += thanksText.getLayoutBounds().getHeight() + 20;

            // Copyright
            copyrText.setTranslateY(iy - copyrText.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, copyrText);

            // --- BACK button below card ---
            y += cardH + 24;
            back.setTranslateY(y - back.getLayoutBounds().getMinY());
            back.setCursor(javafx.scene.Cursor.HAND);
            FxglUi.addTextCenteredX(scene, back);
            backBtn[0] = back;

            latch.countDown();
        });
        await(latch);
        return backBtn[0];
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
