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
import sk.tuke.gamestudio.entity.BestLevelResult;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.game.logicalmazes.ui.ProfileOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.BestResultService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

@Profile("fxgl")
@Component
public class ProfilePage {
    private static final ProfileOption[] AUTHORIZED_ACTIONS = {
            ProfileOption.CHANGE_PASSWORD, ProfileOption.LOGOUT, ProfileOption.BACK
    };
    private static final ProfileOption[] GUEST_ACTIONS = {
            ProfileOption.LOGIN, ProfileOption.REGISTER, ProfileOption.BACK
    };

    private static final DateTimeFormatter MEMBER_FMT =
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    private static final double CARD_W   = 440;
    private static final double CARD_PAD = 28;

    private final BestResultService bestResultService;
    private final Selector selector;
    private final ParallaxBackground bg;

    public ProfilePage(
            BestResultService bestResultService,
            Selector selector,
            @Qualifier("profileBackground") ParallaxBackground bg
    ) {
        this.bestResultService = bestResultService;
        this.selector = selector;
        this.bg = bg;
    }

    public ProfileOption showGuest() {
        Text[] indicatorHolder = new Text[1];
        List<Text> buttons = buildGuestUI(indicatorHolder);
        return selector.select(GUEST_ACTIONS, buttons, FxglUi.DEFAULT_ACTIVATION_COLOR, indicatorHolder[0]);
    }

    public ProfileOption showAuthorized(User user) {
        Text[] indicatorHolder = new Text[1];
        List<Text> buttons = buildAuthorizedUI(user, indicatorHolder);
        return selector.select(AUTHORIZED_ACTIONS, buttons, FxglUi.DEFAULT_ACTIVATION_COLOR, indicatorHolder[0]);
    }

    // ─── Authorized ──────────────────────────────────────────────────────────

    private List<Text> buildAuthorizedUI(User user, Text[] indicatorHolder) {
        List<Text> buttons = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double appW = FXGL.getAppWidth();
            double appH = FXGL.getAppHeight();
            double cardX = (appW - CARD_W) / 2.0;

            // Gather data
            Integer overallScore = bestResultService.getBestOverallScore(user.getId());
            Integer rank         = bestResultService.getUserLeaderboardPosition(user.getId());
            List<BestLevelResult> results = bestResultService.getBestResultsByUserId(user.getId());
            int completed = results != null ? results.size() : 0;
            int total     = Level.values().length;
            boolean hasMember = user.getCreatedAt() != null;

            // Create content nodes to measure heights
            Text nameNode    = FxglUi.createText(user.getName(), 20, FxglUi.DEFAULT_TITLE_COLOR);
            Text scoreLabel  = FxglUi.createText("SCORE",  8, FxglUi.DIM_WHITE);
            Text scoreValue  = FxglUi.createText(String.valueOf(overallScore != null ? overallScore : 0), 14, Color.WHITE);
            Text rankLabel   = FxglUi.createText("RANK",   8, FxglUi.DIM_WHITE);
            Color rankColor  = rank == null ? Color.WHITE
                    : rank == 1 ? Color.rgb(245, 197, 24)
                    : rank == 2 ? Color.rgb(192, 192, 192)
                    : rank == 3 ? Color.rgb(205, 127, 50)
                    : Color.rgb(145, 205, 255);
            Text rankValue   = FxglUi.createText(rank != null ? "#" + rank : "-", 14, rankColor);
            Text levelsLabel = FxglUi.createText("LEVELS", 8, FxglUi.DIM_WHITE);
            Text levelsValue = FxglUi.createText(completed + " / " + total, 14, Color.WHITE);

            Text memberText  = hasMember
                    ? FxglUi.createText("Member since " + user.getCreatedAt().format(MEMBER_FMT),
                            8, Color.rgb(255, 255, 255, 0.35))
                    : null;

            String[] btnLabels = {"CHANGE PASSWORD", "LOGOUT", "BACK"};
            Color[]  btnColors = {FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DANGER_COLOR, FxglUi.DEFAULT_BUTTON_COLOR};
            Text[] navBtns = new Text[2]; // inside card: CHANGE PASSWORD, LOGOUT
            Text   backBtn = FxglUi.createText(btnLabels[2], FxglUi.BUTTON_SIZE, btnColors[2]);
            for (int i = 0; i < 2; i++)
                navBtns[i] = FxglUi.createText(btnLabels[i], FxglUi.NAV_BTN_SIZE, btnColors[i]);

            // Card content height
            double statLH = Math.max(scoreLabel.getLayoutBounds().getHeight(),
                                     rankLabel.getLayoutBounds().getHeight());
            double statVH = scoreValue.getLayoutBounds().getHeight();
            double statRowH = statLH + 6 + statVH; // label + gap + value
            double sep = 2, sepGap = 10;

            double cardContentH = nameNode.getLayoutBounds().getHeight() + 6
                    + sep + sepGap                                   // grad sep 1
                    + statRowH + 10                                  // stats
                    + (hasMember ? memberText.getLayoutBounds().getHeight() + 6 : 0)
                    + sep + sepGap                                   // grad sep 2
                    + navBtns[0].getLayoutBounds().getHeight() + 10  // change pw
                    + navBtns[1].getLayoutBounds().getHeight();       // logout
            double cardH = CARD_PAD * 2 + cardContentH;

            // Block layout (margin-top: -160 → offset=80)
            Text t1 = FxglUi.createSubTitle("YOUR",    FxglUi.DEFAULT_TITLE_COLOR);
            Text t2 = FxglUi.createSubTitle("PROFILE", FxglUi.DEFAULT_TITLE_COLOR);
            double h1 = t1.getLayoutBounds().getHeight();
            double h2 = t2.getLayoutBounds().getHeight();
            double bH = backBtn.getLayoutBounds().getHeight();
            double blockH = h1 + 8 + h2 + 32 + cardH + 24 + bH;
            double blockTop = (appH - blockH) / 2.0 - 80;

            // Titles
            double y = blockTop;
            t1.setTranslateY(y - t1.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t1, -8);
            y += h1 + 8;
            t2.setTranslateY(y - t2.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t2, 8);
            y += h2 + 32;

            // Card
            Rectangle card = FxglUi.createCardPanel(cardX, y, CARD_W, cardH);
            scene.addUINode(card);

            double iy = y + CARD_PAD;

            // Username
            nameNode.setTranslateY(iy - nameNode.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, nameNode);
            iy += nameNode.getLayoutBounds().getHeight() + 6;

            // Sep 1
            scene.addUINode(FxglUi.createGradientSep(cardX + CARD_W * 0.2, iy, CARD_W * 0.6));
            iy += sep + sepGap;

            // Stats row — 3 columns centred
            double col1 = appW * 0.5 - 120;
            double col2 = appW * 0.5;
            double col3 = appW * 0.5 + 120;
            placeStatCol(scene, scoreLabel,  scoreValue,  col1, iy, statLH, statVH);
            placeStatCol(scene, rankLabel,   rankValue,   col2, iy, statLH, statVH);
            placeStatCol(scene, levelsLabel, levelsValue, col3, iy, statLH, statVH);
            iy += statRowH + 10;

            // Member since
            if (hasMember) {
                memberText.setTranslateY(iy - memberText.getLayoutBounds().getMinY());
                FxglUi.addTextCenteredX(scene, memberText);
                iy += memberText.getLayoutBounds().getHeight() + 6;
            }

            // Sep 2
            scene.addUINode(FxglUi.createGradientSep(cardX + CARD_W * 0.2, iy, CARD_W * 0.6));
            iy += sep + sepGap;

            // Nav buttons inside card
            for (int i = 0; i < 2; i++) {
                navBtns[i].setTranslateY(iy - navBtns[i].getLayoutBounds().getMinY());
                navBtns[i].setCursor(javafx.scene.Cursor.HAND);
                FxglUi.addTextCenteredX(scene, navBtns[i]);
                iy += navBtns[i].getLayoutBounds().getHeight() + (i < 1 ? 10 : 0);
                buttons.add(navBtns[i]);
            }

            // BACK button below card
            y += cardH + 24;
            backBtn.setTranslateY(y - backBtn.getLayoutBounds().getMinY());
            backBtn.setCursor(javafx.scene.Cursor.HAND);
            FxglUi.addTextCenteredX(scene, backBtn);
            buttons.add(backBtn);

            // ">" indicator
            indicatorHolder[0] = addIndicator(scene, buttons);
            latch.countDown();
        });
        await(latch);
        return buttons;
    }

    // ─── Guest ────────────────────────────────────────────────────────────────

    private List<Text> buildGuestUI(Text[] indicatorHolder) {
        List<Text> buttons = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double appH = FXGL.getAppHeight();

            Text t1 = FxglUi.createSubTitle("LOGIN",    FxglUi.DEFAULT_TITLE_COLOR);
            Text t2 = FxglUi.createSubTitle("REGISTER", FxglUi.DEFAULT_TITLE_COLOR);

            String[] labels = {"LOGIN", "REGISTER", "BACK"};
            Text[] btns = new Text[labels.length];
            for (int i = 0; i < labels.length; i++)
                btns[i] = FxglUi.createText(labels[i], FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);

            double h1 = t1.getLayoutBounds().getHeight();
            double h2 = t2.getLayoutBounds().getHeight();
            double btnH = btns[0].getLayoutBounds().getHeight();
            double blockH = h1 + 8 + h2 + 32 + btns.length * btnH + (btns.length - 1) * 15.0;
            double blockTop = (appH - blockH) / 2.0 - 70; // menuOffset=140

            double y = blockTop;
            t1.setTranslateY(y - t1.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t1, -8);
            y += h1 + 8;
            t2.setTranslateY(y - t2.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, t2, 8);
            y += h2 + 32;

            for (int i = 0; i < btns.length; i++) {
                double top = y + i * (btnH + 15);
                btns[i].setTranslateY(top - btns[i].getLayoutBounds().getMinY());
                btns[i].setCursor(javafx.scene.Cursor.HAND);
                FxglUi.addTextCenteredX(scene, btns[i]);
                buttons.add(btns[i]);
            }

            indicatorHolder[0] = addIndicator(scene, buttons);
            latch.countDown();
        });
        await(latch);
        return buttons;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void placeStatCol(GameScene scene,
                               Text label, Text value,
                               double colCenterX, double topY,
                               double labelH, double valueH) {
        label.setTranslateX(colCenterX - label.getLayoutBounds().getWidth() / 2.0);
        label.setTranslateY(topY - label.getLayoutBounds().getMinY());
        scene.addUINode(label);

        double vTopY = topY + labelH + 6;
        value.setTranslateX(colCenterX - value.getLayoutBounds().getWidth() / 2.0);
        value.setTranslateY(vTopY - value.getLayoutBounds().getMinY());
        scene.addUINode(value);
    }

    private Text addIndicator(GameScene scene, List<Text> buttons) {
        if (buttons.isEmpty()) return null;
        Text ind = FxglUi.createText(">", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_TITLE_COLOR);
        scene.addUINode(ind);
        ind.setTranslateX(buttons.get(0).getTranslateX() - 28);
        ind.setTranslateY(buttons.get(0).getTranslateY());
        return ind;
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
