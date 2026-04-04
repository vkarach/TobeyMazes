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
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Profile("fxgl")
@Component
public class LevelSelectPage {
    private final BestResultService bestResultService;
    private final Selector selector;
    private final ParallaxBackground bg;

    public LevelSelectPage(
            BestResultService bestResultService,
            Selector selector,
            @Qualifier("mainMenuBackground") ParallaxBackground bg
    ) {
        this.bestResultService = bestResultService;
        this.selector = selector;
        this.bg = bg;
    }

    public Level show() {
        CompletableFuture<Level> result = new CompletableFuture<>();
        Thread quitWatcher = buildUI(result);
        try {
            return result.get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            quitWatcher.interrupt();
        }
    }

    public Thread buildUI(CompletableFuture<Level> result) {
        int SQUARE_SIZE = 100;
        int SQUARE_PER_ROW = 4;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            Text text1 = FxglUi.createTitle("SELECT", FxglUi.DEFAULT_TITLE_COLOR);
            double y = FxglUi.DEFAULT_PAD + text1.getLayoutBounds().getHeight();
            text1.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text1, -50);
            y*=2;

            Text text2 = FxglUi.createTitle("LEVEL", FxglUi.DEFAULT_TITLE_COLOR);
            text2.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, text2, +50);
            y += FxglUi.DEFAULT_PAD * 2;

            Level[] levels = Level.values();
            for (int i = 0; i < levels.length; i++) {
                Level level = levels[i];
                if (i != 0 && i % SQUARE_PER_ROW == 0) {
                    y += SQUARE_SIZE + FxglUi.DEFAULT_PAD;
                }
                Rectangle rect = createSquare(SQUARE_SIZE);
                rect.setOnMouseClicked(e ->
                    result.complete(level)
                );
                rect.setOnMouseEntered(e -> rect.setFill(Color.BLUE));
                rect.setOnMouseExited(e -> rect.setFill(Color.DARKBLUE));
                rect.setCursor(javafx.scene.Cursor.HAND);

                double rowWidth = SQUARE_PER_ROW * SQUARE_SIZE + (SQUARE_PER_ROW - 1) * FxglUi.DEFAULT_PAD;
                double startX = (FXGL.getAppWidth() - rowWidth) / 2.0;

                double x = startX + (i % SQUARE_PER_ROW) * (SQUARE_SIZE + FxglUi.DEFAULT_PAD);
                rect.setTranslateX(x);
                rect.setTranslateY(y);
                scene.addUINode(rect);

                Text levelName = FxglUi.createText(level.name(), 15, Color.WHITE);
                levelName.setTranslateX(x);
                levelName.setTranslateY(y - 5);
                scene.addUINode(levelName);
            }

            Text back = FxglUi.createText("Back", 20, FxglUi.DEFAULT_BUTTON_COLOR);
            back.setCursor(javafx.scene.Cursor.HAND);
            back.setOnMouseEntered(e -> back.setFill(FxglUi.DEFAULT_ACTIVATION_COLOR));
            back.setOnMouseExited(e -> back.setFill(FxglUi.DEFAULT_BUTTON_COLOR));
            back.setOnMouseClicked(e -> result.complete(null));
            back.setTranslateY(FXGL.getAppHeight() - FxglUi.DEFAULT_PAD);
            FxglUi.addTextCenteredX(scene, back);

            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return selector.onQuit(() -> result.complete(null));
    }
    private Rectangle createSquare(int sideSize) {
        Rectangle rect = new Rectangle(sideSize, sideSize);
        rect.setFill(Color.DARKBLUE);
        rect.setStroke(Color.WHITE);
        rect.setStrokeWidth(2);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        return rect;
    }
}
