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
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Profile("fxgl")
@Component
public class LeaderboardPage {
    private static final Color GOLD   = Color.rgb(245, 197, 24);
    private static final Color SILVER = Color.rgb(145, 205, 255);
    private static final Color BRONZE = Color.rgb(205, 127, 50);
    private static final Color ME     = Color.rgb(145, 205, 255);
    private static final Color ME_BG  = Color.rgb(245, 197, 24, 0.10);

    private static final double CARD_W   = 480;
    private static final double CARD_PAD = 28;

    private final BestResultService bestResultService;
    private final Selector selector;
    private final ParallaxBackground bg;

    public LeaderboardPage(
           BestResultService bestResultService,
           Selector selector,
           @Qualifier("leaderboardBackground") ParallaxBackground bg
    ) {
        this.bestResultService = bestResultService;
        this.selector = selector;
        this.bg = bg;
    }

    public void show(User currentUser) {
        Text backBtn = buildUI(currentUser);
        selector.waitForConfirm(backBtn, FxglUi.DEFAULT_ACTIVATION_COLOR);
    }

    private Text buildUI(User currentUser) {
        Text[] backHolder = new Text[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double appW = FXGL.getAppWidth();
            double appH = FXGL.getAppHeight();
            double cardX = (appW - CARD_W) / 2.0;

            List<UserScore> top = bestResultService.getTopByScore();

            // Column X positions within card (matching CSS flex layout)
            double rankX  = cardX + 40;
            double nameX  = cardX + 96;
            double scoreRightX = cardX + CARD_W - 40; // right-align score

            // Measure row height at 10px
            Text sampleRow = FxglUi.createText("#1", 10, Color.WHITE);
            double rowH  = sampleRow.getLayoutBounds().getHeight();
            double rowGap = 5;

            // Header at 9px
            Text sampleHdr = FxglUi.createText("#", 9, FxglUi.DIM_WHITE);
            double hdrH = sampleHdr.getLayoutBounds().getHeight();

            int rowCount = top.size();
            int extra = 0;
            if (currentUser != null) {
                boolean inTop = top.stream().anyMatch(e -> e.userId().equals(currentUser.getId()));
                Integer pos = inTop ? null : bestResultService.getUserLeaderboardPosition(currentUser.getId());
                if (pos != null && pos > 10) extra = 2; // "..." + me row
            }

            double cardContentH = hdrH + 8        // header + pad
                    + 2 + 8                        // separator + gap
                    + rowCount * (rowH + rowGap);
            if (extra > 0) cardContentH += (rowH + rowGap) * extra;
            double cardH = CARD_PAD * 2 + cardContentH;

            // Title nodes for block height calculation
            Text t1   = FxglUi.createSubTitle("LEADER", FxglUi.DEFAULT_TITLE_COLOR);
            Text t2   = FxglUi.createSubTitle("BOARD",  FxglUi.DEFAULT_TITLE_COLOR);
            Text back = FxglUi.createText("BACK", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            double h1 = t1.getLayoutBounds().getHeight();
            double h2 = t2.getLayoutBounds().getHeight();
            double bH = back.getLayoutBounds().getHeight();
            double blockH = h1 + 8 + h2 + 36 + cardH + 24 + bH;
            double blockTop = (appH - blockH) / 2.0 - 25; // menuOffset=50

            // Titles
            double y = blockTop;
            t1.setTranslateY(y - t1.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t1, -8);
            y += h1 + 8;
            t2.setTranslateY(y - t2.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t2, 8);
            y += h2 + 36;

            // Card panel
            Rectangle card = FxglUi.createCardPanel(cardX, y, CARD_W, cardH);
            scene.addUINode(card);

            double iy = y + CARD_PAD;

            // Header row
            placeRowText(scene, "#",      9, FxglUi.DIM_WHITE, rankX,  iy, false, 0);
            placeRowText(scene, "PLAYER", 9, FxglUi.DIM_WHITE, nameX,  iy, false, 0);
            placeRowText(scene, "SCORE",  9, FxglUi.DIM_WHITE, scoreRightX, iy, true, 0);
            iy += hdrH + 8;

            // Gradient separator
            scene.addUINode(FxglUi.createGradientSep(cardX + 20, iy, CARD_W - 40));
            iy += 2 + 8;

            // Data rows
            int userRank = -1;
            int userScore = 0;
            for (int i = 0; i < top.size(); i++) {
                UserScore entry = top.get(i);
                int rank = i + 1;
                boolean isMe = currentUser != null && entry.userId().equals(currentUser.getId());
                if (isMe) { userRank = rank; userScore = entry.totalScore(); }

                Color rowColor = isMe ? ME
                        : rank == 1 ? GOLD
                        : rank == 2 ? SILVER
                        : rank == 3 ? BRONZE
                        : Color.rgb(255, 255, 255, 0.85);

                if (isMe) {
                    Rectangle meBg = new Rectangle(CARD_W - 16, rowH + 8, ME_BG);
                    meBg.setTranslateX(cardX + 8);
                    meBg.setTranslateY(iy - 4);
                    meBg.setArcWidth(3);
                    meBg.setArcHeight(3);
                    scene.addUINode(meBg);
                }

                placeRowText(scene, "#" + rank,                10, rowColor, rankX,        iy, false, rowH);
                placeRowText(scene, truncate(entry.userName()), 10, rowColor, nameX,        iy, false, rowH);
                placeRowText(scene, String.valueOf(entry.totalScore()), 10, rowColor, scoreRightX, iy, true, rowH);
                iy += rowH + rowGap;
            }

            // User outside top 10
            if (currentUser != null && userRank == -1) {
                Integer pos   = bestResultService.getUserLeaderboardPosition(currentUser.getId());
                Integer score = bestResultService.getBestOverallScore(currentUser.getId());
                if (pos != null && pos > 10) {
                    Text dots = FxglUi.createText("...", 10, Color.rgb(255, 255, 255, 0.3));
                    dots.setTranslateX(nameX);
                    dots.setTranslateY(iy - dots.getLayoutBounds().getMinY());
                    scene.addUINode(dots);
                    iy += rowH + rowGap;

                    Rectangle meBg = new Rectangle(CARD_W - 16, rowH + 8, ME_BG);
                    meBg.setTranslateX(cardX + 8);
                    meBg.setTranslateY(iy - 4);
                    meBg.setArcWidth(3); meBg.setArcHeight(3);
                    scene.addUINode(meBg);
                    placeRowText(scene, "#" + pos,                    10, ME, rankX,        iy, false, rowH);
                    placeRowText(scene, currentUser.getName(),         10, ME, nameX,        iy, false, rowH);
                    placeRowText(scene, String.valueOf(score != null ? score : 0), 10, ME, scoreRightX, iy, true, rowH);
                    iy += rowH + rowGap;
                }
            }

            // BACK button
            y += cardH + 24;
            back.setTranslateY(y - back.getLayoutBounds().getMinY());
            back.setCursor(javafx.scene.Cursor.HAND);
            FxglUi.addTextCenteredX(scene, back);
            backHolder[0] = back;

            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return backHolder[0];
    }

    private void placeRowText(GameScene scene, String text, int size, Color color,
                               double x, double y, boolean rightAlign, double refH) {
        Text t = FxglUi.createText(text, size, color);
        double baseline = y - t.getLayoutBounds().getMinY();
        if (refH > 0) {
            // Vertically center within the row height
            baseline = y + (refH - t.getLayoutBounds().getHeight()) / 2.0
                    - t.getLayoutBounds().getMinY();
        }
        t.setTranslateY(baseline);
        if (rightAlign) {
            t.setTranslateX(x - t.getLayoutBounds().getWidth());
        } else {
            t.setTranslateX(x);
        }
        scene.addUINode(t);
    }

    private String truncate(String s) {
        return s.length() > 18 ? s.substring(0, 16) + ".." : s;
    }
}
