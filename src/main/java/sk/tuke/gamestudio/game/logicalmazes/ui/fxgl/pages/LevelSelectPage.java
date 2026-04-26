package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglInput;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mirrors templates/levels.html + static/js/levels.js + static/css/levels.css.
 * Grid (4 × 3), keyboard navigation (arrow keys), level confirm modal with
 * a 260×260 preview image, stats, BACK/START buttons.
 */
@Profile("fxgl")
@Component
public class LevelSelectPage {
    private static final int CARD_W        = 180;
    private static final int CARD_H        = 96;
    private static final int CARD_GAP      = 12;
    private static final int CARDS_PER_ROW = 4;
    private static final int PREVIEW_SIZE  = 260;

    private final BestResultService bestResultService;
    private final Selector selector;
    private final FxglInput input;
    private final ParallaxBackground bg;

    public LevelSelectPage(
            BestResultService bestResultService,
            Selector selector,
            FxglInput input,
            @Qualifier("mainMenuBackground") ParallaxBackground bg
    ) {
        this.bestResultService = bestResultService;
        this.selector = selector;
        this.input = input;
        this.bg = bg;
    }

    public Level show(User currentUser) {
        selector.ensureBindings();
        while (true) {
            PickResult picked = pickLevel();
            if (picked == null) return null;
            ConfirmResult result = confirmLevel(picked.level, currentUser);
            if (result == ConfirmResult.START) return picked.level;
            // BACK → loop and keep same selection as before
        }
    }

    private enum ConfirmResult { START, BACK }

    private record PickResult(Level level, int index) {}

    // ─── Phase 1: card grid with keyboard navigation ──────────────────────

    private PickResult pickLevel() {
        Level[] levels = Level.values();
        int rowCount = (levels.length + CARDS_PER_ROW - 1) / CARDS_PER_ROW;
        double gridW = CARDS_PER_ROW * CARD_W + (CARDS_PER_ROW - 1) * CARD_GAP;
        double startX = (FXGL.getAppWidth() - gridW) / 2.0;

        Rectangle[] cards = new Rectangle[levels.length];
        Color[] cardBorder = new Color[levels.length];
        AtomicInteger selected = new AtomicInteger(0);
        CompletableFuture<PickResult> result = new CompletableFuture<>();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double appH = FXGL.getAppHeight();

            Text title = FxglUi.createText("SELECT LEVEL", 20, FxglUi.DEFAULT_TITLE_COLOR);
            double titleH = title.getLayoutBounds().getHeight();
            double gridH  = rowCount * CARD_H + (rowCount - 1) * CARD_GAP;
            Text backLink = FxglUi.createText("< Back to Menu", 10, Color.rgb(255, 255, 255, 0.5));
            double backLH = backLink.getLayoutBounds().getHeight();
            double blockH = titleH + 32 + gridH + 24 + backLH;
            double blockTop = (appH - blockH) / 2.0;

            double y = blockTop;
            title.setTranslateY(y - title.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, title);
            y += titleH + 32;

            double rowY = y;
            for (int i = 0; i < levels.length; i++) {
                if (i != 0 && i % CARDS_PER_ROW == 0) rowY += CARD_H + CARD_GAP;
                double x = startX + (i % CARDS_PER_ROW) * (CARD_W + CARD_GAP);
                cardBorder[i] = diffColor(levels[i].getDifficulty());
                cards[i] = buildCard(scene, levels[i], x, rowY, i, selected, cards, cardBorder, result);
            }

            double backY = y + gridH + 24;
            backLink.setTranslateY(backY - backLink.getLayoutBounds().getMinY());
            backLink.setOnMouseClicked(e -> result.complete(null));
            FxglUi.addTextCenteredX(scene, backLink);
            FxglUi.wireLinkHover(backLink, Color.rgb(255, 255, 255, 0.5), Color.WHITE);

            paintCard(cards, selected.get(), cardBorder);
            latch.countDown();
        });
        await(latch);

        // Keyboard navigation thread — arrow keys on a 2D grid, ENTER to pick.
        Thread kbd = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                InputType in = input.getInput();
                if (in == null) continue;
                int sel = selected.get();
                int newSel = sel;
                switch (in) {
                    case LEFT -> {
                        int row = sel / CARDS_PER_ROW;
                        int rowStart = row * CARDS_PER_ROW;
                        if (sel > rowStart) newSel = sel - 1;
                    }
                    case RIGHT -> {
                        int row = sel / CARDS_PER_ROW;
                        int rowEnd = Math.min((row + 1) * CARDS_PER_ROW, levels.length) - 1;
                        if (sel < rowEnd) newSel = sel + 1;
                    }
                    case UP -> {
                        int next = sel - CARDS_PER_ROW;
                        if (next >= 0) newSel = next;
                    }
                    case DOWN -> {
                        int next = sel + CARDS_PER_ROW;
                        if (next < levels.length) newSel = next;
                    }
                    case ENTER -> {
                        result.complete(new PickResult(levels[selected.get()], selected.get()));
                        return;
                    }
                    case QUIT -> {
                        result.complete(null);
                        return;
                    }
                    default -> {}
                }
                if (newSel != sel) {
                    final int finalSel = newSel;
                    selected.set(finalSel);
                    Platform.runLater(() -> paintCard(cards, finalSel, cardBorder));
                }
            }
        }, "levels-kbd");
        kbd.setDaemon(true);
        kbd.start();

        try {
            return result.get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            kbd.interrupt();
        }
    }

    private Rectangle buildCard(GameScene scene, Level level, double x, double y,
                                 int index, AtomicInteger selected,
                                 Rectangle[] cards, Color[] cardBorder,
                                 CompletableFuture<PickResult> result) {
        Color diffColor = diffColor(level.getDifficulty());
        Color panelColor = Color.rgb(20, 10, 5, 0.82);
        Color borderColor = Color.rgb(255, 255, 255, 0.23);

        Rectangle card = new Rectangle(CARD_W, CARD_H);
        card.setFill(panelColor);
        card.setStroke(borderColor);
        card.setStrokeWidth(1);
        card.setArcWidth(0); card.setArcHeight(0);
        card.setTranslateX(x);
        card.setTranslateY(y);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseEntered(e -> {
            selected.set(index);
            paintCard(cards, index, cardBorder);
        });
        card.setOnMouseClicked(e -> result.complete(new PickResult(level, index)));
        scene.addUINode(card);

        // Difficulty top stripe (3px) matches .level-card::before
        Rectangle stripe = new Rectangle(CARD_W, 3, diffColor);
        stripe.setTranslateX(x);
        stripe.setTranslateY(y);
        stripe.setMouseTransparent(true);
        scene.addUINode(stripe);

        Text numText = FxglUi.createText("#" + level.getId(), 8, Color.rgb(255, 255, 255, 0.5));
        numText.setMouseTransparent(true);
        numText.setTranslateX(x + (CARD_W - numText.getLayoutBounds().getWidth()) / 2.0);
        numText.setTranslateY(y + 22 - numText.getLayoutBounds().getMinY());
        scene.addUINode(numText);

        Text titleText = FxglUi.createText(level.getTitle(), 9, Color.WHITE);
        titleText.setMouseTransparent(true);
        titleText.setTranslateX(x + (CARD_W - titleText.getLayoutBounds().getWidth()) / 2.0);
        titleText.setTranslateY(y + CARD_H / 2.0 + 4 - titleText.getLayoutBounds().getMinY()
                - titleText.getLayoutBounds().getHeight() / 2.0);
        scene.addUINode(titleText);

        Text diffText = FxglUi.createText(level.getDifficulty().name(), 7, diffColor);
        diffText.setMouseTransparent(true);
        diffText.setTranslateX(x + (CARD_W - diffText.getLayoutBounds().getWidth()) / 2.0);
        diffText.setTranslateY(y + CARD_H - 14 - diffText.getLayoutBounds().getMinY());
        scene.addUINode(diffText);

        return card;
    }

    /** Highlight the selected card (matches .level-card:hover). */
    private void paintCard(Rectangle[] cards, int selected, Color[] borders) {
        for (int i = 0; i < cards.length; i++) {
            boolean active = (i == selected);
            cards[i].setFill(active ? Color.rgb(20, 10, 5, 0.95) : Color.rgb(20, 10, 5, 0.82));
            cards[i].setStroke(active ? borders[i] : Color.rgb(255, 255, 255, 0.23));
        }
    }

    // ─── Phase 2: confirm modal (260×260 preview + info + buttons) ───────

    private ConfirmResult confirmLevel(Level level, User currentUser) {
        CompletableFuture<ConfirmResult> choice = new CompletableFuture<>();
        CountDownLatch latch = new CountDownLatch(1);
        FxglUi.Modal[] modalHolder = new FxglUi.Modal[1];
        AtomicInteger modalSel = new AtomicInteger(1); // 0 = BACK, 1 = START (default)
        Text[] backBtnHolder = new Text[1];
        Text[] startBtnHolder = new Text[1];

        Long bestTime     = currentUser != null ? bestResultService.getBestTime(currentUser.getId(), level.getId())  : null;
        Integer bestScore = currentUser != null ? bestResultService.getBestScore(currentUser.getId(), level.getId()) : null;

        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            double W = FXGL.getAppWidth(), H = FXGL.getAppHeight();

            // Match .level-modal: flex row with preview + info column, padding 32 × 40
            double colGap = 32, padX = 40, padY = 32;
            double rightW = 260;
            double panelW = padX * 2 + PREVIEW_SIZE + colGap + rightW;
            double panelH = padY * 2 + PREVIEW_SIZE;

            Group content = new Group();
            content.setTranslateX(W / 2.0);
            content.setTranslateY(H / 2.0 - 50); // matches margin-top: -50px in .level-modal
            Color diffC = diffColor(level.getDifficulty());

            // Panel
            Rectangle panel = new Rectangle(panelW, panelH);
            panel.setFill(FxglUi.MODAL_PANEL_FILL);
            panel.setStroke(FxglUi.MODAL_PANEL_STROKE);
            panel.setStrokeWidth(FxglUi.MODAL_PANEL_STROKE_WIDTH);
            panel.setArcWidth(FxglUi.MODAL_PANEL_ARC);
            panel.setArcHeight(FxglUi.MODAL_PANEL_ARC);
            panel.setTranslateX(-panelW / 2.0);
            panel.setTranslateY(-panelH / 2.0);
            content.getChildren().add(panel);

            // Left column: preview image
            double previewX = -panelW / 2.0 + padX;
            double previewY = -panelH / 2.0 + padY;
            try {
                Image preview = new Image(FileReader.getInputStream(
                        "static/img/levels/level_" + level.getId() + ".png"));
                ImageView iv = new ImageView(preview);
                iv.setSmooth(false);
                iv.setPreserveRatio(true);
                iv.setFitWidth(PREVIEW_SIZE);
                iv.setFitHeight(PREVIEW_SIZE);
                iv.setTranslateX(previewX);
                iv.setTranslateY(previewY);
                content.getChildren().add(iv);
            }
            catch (Exception ex) {
                Rectangle ph = new Rectangle(PREVIEW_SIZE, PREVIEW_SIZE, Color.rgb(40, 20, 10));
                ph.setTranslateX(previewX);
                ph.setTranslateY(previewY);
                content.getChildren().add(ph);
            }

            // Right column x range
            double rightX = previewX + PREVIEW_SIZE + colGap;
            double rightCX = rightX + rightW / 2.0;

            // Header: info-label (title) + info-diff (difficulty)
            Text titleNode = FxglUi.createText(level.getTitle(), 20, FxglUi.DEFAULT_TITLE_COLOR);
            double titleY = previewY + 16;
            titleNode.setTranslateX(rightCX - titleNode.getLayoutBounds().getWidth() / 2.0);
            titleNode.setTranslateY(titleY - titleNode.getLayoutBounds().getMinY());
            content.getChildren().add(titleNode);

            double afterTitle = titleY + titleNode.getLayoutBounds().getHeight() + 8;
            Text diffNode = FxglUi.createText(level.getDifficulty().name(), 11, diffC);
            diffNode.setTranslateX(rightCX - diffNode.getLayoutBounds().getWidth() / 2.0);
            diffNode.setTranslateY(afterTitle - diffNode.getLayoutBounds().getMinY());
            content.getChildren().add(diffNode);

            double afterDiff = afterTitle + diffNode.getLayoutBounds().getHeight() + 14;

            // Separator
            Rectangle sep1 = FxglUi.createGradientSep(rightX + rightW * 0.1, afterDiff, rightW * 0.8);
            content.getChildren().add(sep1);
            double afterSep1 = afterDiff + 2 + 14;

            // Stats: BEST TIME | BEST SCORE
            Text timeLabel  = FxglUi.createText("BEST TIME",  9, Color.rgb(255, 255, 255, 0.4));
            Text timeVal    = FxglUi.createText(formatTime(bestTime), 14, FxglUi.DEFAULT_TITLE_COLOR);
            Text scoreLabel = FxglUi.createText("BEST SCORE", 9, Color.rgb(255, 255, 255, 0.4));
            Text scoreVal   = FxglUi.createText(bestScore != null ? String.valueOf(bestScore) : "\u2014", 14, FxglUi.DEFAULT_TITLE_COLOR);

            double statGap = 32;
            double lblH = timeLabel.getLayoutBounds().getHeight();
            double statTimeCX  = rightX + (rightW - statGap) / 4.0 + 12;
            double statScoreCX = rightX + rightW - ((rightW - statGap) / 4.0 + 12);

            timeLabel.setTranslateX(statTimeCX - timeLabel.getLayoutBounds().getWidth() / 2.0);
            timeLabel.setTranslateY(afterSep1 - timeLabel.getLayoutBounds().getMinY());
            scoreLabel.setTranslateX(statScoreCX - scoreLabel.getLayoutBounds().getWidth() / 2.0);
            scoreLabel.setTranslateY(afterSep1 - scoreLabel.getLayoutBounds().getMinY());
            content.getChildren().addAll(timeLabel, scoreLabel);
            double afterLbl = afterSep1 + lblH + 8;

            timeVal.setTranslateX(statTimeCX - timeVal.getLayoutBounds().getWidth() / 2.0);
            timeVal.setTranslateY(afterLbl - timeVal.getLayoutBounds().getMinY());
            scoreVal.setTranslateX(statScoreCX - scoreVal.getLayoutBounds().getWidth() / 2.0);
            scoreVal.setTranslateY(afterLbl - scoreVal.getLayoutBounds().getMinY());
            content.getChildren().addAll(timeVal, scoreVal);
            double afterVal = afterLbl + timeVal.getLayoutBounds().getHeight() + 14;

            // Separator 2
            Rectangle sep2 = FxglUi.createGradientSep(rightX + rightW * 0.1, afterVal, rightW * 0.8);
            content.getChildren().add(sep2);
            double afterSep2 = afterVal + 2 + 20;

            // Actions: BACK | START (nav-btn)
            Text backBtn  = FxglUi.createText("BACK",  FxglUi.NAV_BTN_SIZE, Color.rgb(255, 255, 255, 0.4));
            Text startBtn = FxglUi.createText("START", FxglUi.NAV_BTN_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            double actionGap = 24;
            double actionsW  = backBtn.getLayoutBounds().getWidth() + actionGap + startBtn.getLayoutBounds().getWidth();
            double actionsX  = rightCX - actionsW / 2.0;

            backBtn.setTranslateX(actionsX);
            backBtn.setTranslateY(afterSep2 - backBtn.getLayoutBounds().getMinY());
            backBtn.setOnMouseClicked(e -> choice.complete(ConfirmResult.BACK));

            startBtn.setTranslateX(actionsX + backBtn.getLayoutBounds().getWidth() + actionGap);
            startBtn.setTranslateY(afterSep2 - startBtn.getLayoutBounds().getMinY());
            startBtn.setOnMouseClicked(e -> choice.complete(ConfirmResult.START));

            FxglUi.wireMenuButton(backBtn,  Color.rgb(255, 255, 255, 0.4), FxglUi.DEFAULT_ACTIVATION_COLOR);
            FxglUi.wireMenuButton(startBtn, FxglUi.DEFAULT_BUTTON_COLOR,    FxglUi.DEFAULT_ACTIVATION_COLOR);

            content.getChildren().addAll(backBtn, startBtn);
            backBtnHolder[0]  = backBtn;
            startBtnHolder[0] = startBtn;

            // Mouse hover drives keyboard highlight too
            backBtn.setOnMouseEntered(e -> {
                modalSel.set(0);
                paintModalActive(backBtn, startBtn, 0);
            });
            startBtn.setOnMouseEntered(e -> {
                modalSel.set(1);
                paintModalActive(backBtn, startBtn, 1);
            });

            paintModalActive(backBtn, startBtn, modalSel.get());
            modalHolder[0] = FxglUi.openModal(scene, FxglUi.MODAL_OVERLAY_ALPHA, content);
            latch.countDown();
        });
        await(latch);

        // Keyboard: LEFT/RIGHT cycles BACK/START, ENTER activates, ESC/Q closes.
        Thread kbd = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                InputType in = input.getInput();
                if (in == null) continue;
                switch (in) {
                    case LEFT -> {
                        modalSel.set(0);
                        Platform.runLater(() -> paintModalActive(backBtnHolder[0], startBtnHolder[0], 0));
                    }
                    case RIGHT -> {
                        modalSel.set(1);
                        Platform.runLater(() -> paintModalActive(backBtnHolder[0], startBtnHolder[0], 1));
                    }
                    case ENTER -> {
                        choice.complete(modalSel.get() == 0 ? ConfirmResult.BACK : ConfirmResult.START);
                        return;
                    }
                    case QUIT -> {
                        choice.complete(ConfirmResult.BACK);
                        return;
                    }
                    default -> {}
                }
            }
        }, "level-modal-kbd");
        kbd.setDaemon(true);
        kbd.start();

        ConfirmResult res;
        try {
            res = choice.get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            res = ConfirmResult.BACK;
        } finally {
            kbd.interrupt();
        }

        CountDownLatch closed = new CountDownLatch(1);
        Platform.runLater(() -> modalHolder[0].close(closed::countDown));
        try { closed.await(); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        return res;
    }

    private void paintModalActive(Text backBtn, Text startBtn, int activeIndex) {
        Color active = FxglUi.DEFAULT_ACTIVATION_COLOR;
        Color backNormal  = Color.rgb(255, 255, 255, 0.4);
        Color startNormal = FxglUi.DEFAULT_BUTTON_COLOR;
        backBtn.setFill(activeIndex == 0 ? active : backNormal);
        startBtn.setFill(activeIndex == 1 ? active : startNormal);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Color diffColor(Level.Difficulty d) {
        return switch (d) {
            case EASY   -> Color.rgb(93,  206, 110);
            case NORMAL -> Color.rgb(145, 205, 255);
            case MEDIUM -> Color.rgb(245, 166,  35);
            case HARD   -> Color.rgb(255,  92,  92);
        };
    }

    private static String formatTime(Long ms) {
        if (ms == null) return "\u2014";
        double value = ms.doubleValue();
        if (value < 1000) return Math.round(value) + "ms";
        double totalSeconds = value / 1000.0;
        if (totalSeconds < 60) {
            return String.format(totalSeconds < 10 ? "%.2fs" : "%.1fs", totalSeconds);
        }
        long minutes = (long) (totalSeconds / 60);
        long seconds = (long) (totalSeconds % 60);
        return minutes + "m " + String.format("%02ds", seconds);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
