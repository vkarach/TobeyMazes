package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglInput;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.ReviewService;
import sk.tuke.gamestudio.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mirrors templates/review.html + static/css/review.css + static/js/review.js.
 * Layout: "RATE / THE GAME" titles, rv-card card-panel (overall + stars +
 * comment + SUBMIT/thanks), "ALL REVIEWS" + "BACK" menu-btns below.
 * Modal for ALL REVIEWS uses the shared brown-gold design.
 */
@Profile("fxgl")
@Component
public class ReviewPage {

    private static final int STAR_SIZE     = 28;
    private static final int STAR_GAP      = 8; // .rv-star padding 0 4px → 8px total gap
    private static final double STAR_OPACITY_EMPTY   = 0.3;
    private static final double STAR_OPACITY_PREVIEW = 0.75;
    private static final double STAR_OPACITY_FILLED  = 1.0;
    private static final Color  STAR_GLOW = Color.rgb(245, 197, 24, 0.5);
    private static final int    COMMENT_MAX = 100;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    private static Image starImage;
    private static Image starImage() {
        if (starImage == null) {
            starImage = new Image(FileReader.getInputStream("static/img/star.png"));
        }
        return starImage;
    }

    private final ReviewService reviewService;
    private final UserService userService;
    private final FxglInput input;
    private final Selector selector;
    private final ParallaxBackground bg;

    public ReviewPage(
            ReviewService reviewService,
            UserService userService,
            FxglInput input,
            Selector selector,
            @Qualifier("mainMenuBackground") ParallaxBackground bg
    ) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.input = input;
        this.selector = selector;
        this.bg = bg;
    }

    public void show(User currentUser) {
        selector.ensureBindings();
        if (currentUser == null) showGuest();
        else                     showForm(currentUser);
    }

    // ─── Guest view ───────────────────────────────────────────────────────

    private void showGuest() {
        CountDownLatch latch = new CountDownLatch(1);
        Text[] allBtnH = new Text[1];
        Text[] backBtnH = new Text[1];

        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double W = FXGL.getAppWidth();
            buildTitles(scene);

            double cardW = 460;
            double cardX = (W - cardW) / 2.0;
            double cardY = 200;
            double cardPadX = 40, cardPadY = 28;

            // Overall rating section — measure first
            Float overall = reviewService.getOverallRating();
            Text overallLbl = FxglUi.createText("OVERALL", 8, Color.rgb(255, 255, 255, 0.45));
            Text overallVal = FxglUi.createText(overall != null && overall > 0
                    ? String.format(Locale.US, "%.2f / 5", overall)
                    : "no ratings yet", 14, FxglUi.DEFAULT_TITLE_COLOR);
            Text loginHint = FxglUi.createText("Sign in to rate the game", 10, FxglUi.DEFAULT_BUTTON_COLOR);

            double sepH = 2;
            double cardH = cardPadY * 2
                    + overallLbl.getLayoutBounds().getHeight() + 6
                    + overallVal.getLayoutBounds().getHeight() + 20
                    + sepH + 20
                    + loginHint.getLayoutBounds().getHeight();

            Rectangle card = FxglUi.createCardPanel(cardX, cardY, cardW, cardH);
            scene.addUINode(card);

            double iy = cardY + cardPadY;
            overallLbl.setTranslateX(cardX + (cardW - overallLbl.getLayoutBounds().getWidth()) / 2.0);
            overallLbl.setTranslateY(iy - overallLbl.getLayoutBounds().getMinY());
            scene.addUINode(overallLbl);
            iy += overallLbl.getLayoutBounds().getHeight() + 6;

            overallVal.setTranslateX(cardX + (cardW - overallVal.getLayoutBounds().getWidth()) / 2.0);
            overallVal.setTranslateY(iy - overallVal.getLayoutBounds().getMinY());
            scene.addUINode(overallVal);
            iy += overallVal.getLayoutBounds().getHeight() + 20;

            Rectangle sep = FxglUi.createGradientSep(cardX + cardW * 0.2, iy, cardW * 0.6);
            scene.addUINode(sep);
            iy += sepH + 20;

            loginHint.setTranslateX(cardX + (cardW - loginHint.getLayoutBounds().getWidth()) / 2.0);
            loginHint.setTranslateY(iy - loginHint.getLayoutBounds().getMinY());
            scene.addUINode(loginHint);

            // ALL REVIEWS + BACK (menu-btn style) below the card
            double btnY = cardY + cardH + 28;
            Text allBtn = FxglUi.createText("ALL REVIEWS", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            allBtn.setTranslateY(btnY - allBtn.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, allBtn);
            btnY += allBtn.getLayoutBounds().getHeight() + 15;

            Text backBtn = FxglUi.createText("BACK", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            backBtn.setTranslateY(btnY - backBtn.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, backBtn);

            FxglUi.wireMenuButton(allBtn,  FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DEFAULT_ACTIVATION_COLOR);
            FxglUi.wireMenuButton(backBtn, FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DEFAULT_ACTIVATION_COLOR);
            allBtn.setOnMouseClicked(e -> input.push(InputType.ENTER)); // ENTER handled in loop below

            // Indicator ">" (keyboard navigation)
            Text indicator = FxglUi.createText(">", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_TITLE_COLOR);
            scene.addUINode(indicator);
            indicator.setTranslateX(allBtn.getTranslateX() - 28);
            indicator.setTranslateY(allBtn.getTranslateY());

            allBtnH[0]  = allBtn;
            backBtnH[0] = backBtn;
            backBtn.setOnMouseClicked(e -> input.push(InputType.QUIT));

            latch.countDown();
        });
        await(latch);

        // Guest loop: UP/DOWN cycles between ALL REVIEWS ↔ BACK. ENTER acts.
        AtomicInteger sel = new AtomicInteger(0);
        while (true) {
            InputType in = input.getInput();
            switch (in) {
                case UP, DOWN -> sel.set(1 - sel.get());
                case ENTER -> {
                    if (sel.get() == 0) {
                        showAllReviewsModal();
                    }
                    else return;
                }
                case QUIT -> { return; }
                default -> {}
            }
        }
    }

    // ─── Authorized form ──────────────────────────────────────────────────

    private void showForm(User currentUser) {
        Review existing = reviewService.getReview(currentUser.getId());
        AtomicInteger rating = new AtomicInteger(existing != null ? existing.getRating() : 0);
        CountDownLatch latch = new CountDownLatch(1);

        ImageView[] stars = new ImageView[5];
        TextArea[] commentH = new TextArea[1];
        Text[] submitH = new Text[1];
        Text[] thanksH = new Text[1];
        Text[] overallH = new Text[1];

        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double W = FXGL.getAppWidth();
            buildTitles(scene);

            double cardW = 460;
            double cardX = (W - cardW) / 2.0;
            double cardY = 200;
            double cardPadX = 40, cardPadY = 28;

            // Overall rating (top)
            Float overall = reviewService.getOverallRating();
            Text overallLbl = FxglUi.createText("OVERALL", 8, Color.rgb(255, 255, 255, 0.45));
            Text overallVal = FxglUi.createText(overallText(overall), 14, FxglUi.DEFAULT_TITLE_COLOR);
            overallH[0] = overallVal;

            // Card content: overall + sep + stars + comment + submit-row
            double starsH   = STAR_SIZE + 8; // padding 4px top/bottom
            double commentH_ = 64;
            double submitH_  = 24;
            double sepH     = 2;
            double cardH = cardPadY * 2
                    + overallLbl.getLayoutBounds().getHeight() + 6
                    + overallVal.getLayoutBounds().getHeight() + 10
                    + sepH + 14
                    + starsH + 10
                    + commentH_ + 10
                    + submitH_;

            Rectangle card = FxglUi.createCardPanel(cardX, cardY, cardW, cardH);
            scene.addUINode(card);

            double iy = cardY + cardPadY;
            overallLbl.setTranslateX(cardX + (cardW - overallLbl.getLayoutBounds().getWidth()) / 2.0);
            overallLbl.setTranslateY(iy - overallLbl.getLayoutBounds().getMinY());
            scene.addUINode(overallLbl);
            iy += overallLbl.getLayoutBounds().getHeight() + 6;

            overallVal.setTranslateX(cardX + (cardW - overallVal.getLayoutBounds().getWidth()) / 2.0);
            overallVal.setTranslateY(iy - overallVal.getLayoutBounds().getMinY());
            scene.addUINode(overallVal);
            iy += overallVal.getLayoutBounds().getHeight() + 10;

            scene.addUINode(FxglUi.createGradientSep(cardX + cardW * 0.2, iy, cardW * 0.6));
            iy += sepH + 14;

            // Stars row
            double totalStarsW = 5 * STAR_SIZE + 4 * STAR_GAP;
            double starsX = cardX + (cardW - totalStarsW) / 2.0;
            double starsY = iy;
            for (int i = 0; i < 5; i++) {
                ImageView iv = new ImageView(starImage());
                iv.setSmooth(false);
                iv.setPreserveRatio(true);
                iv.setFitWidth(STAR_SIZE);
                iv.setFitHeight(STAR_SIZE);
                iv.setTranslateX(starsX + i * (STAR_SIZE + STAR_GAP));
                iv.setTranslateY(starsY);
                iv.setMouseTransparent(true);
                scene.addUINode(iv);
                stars[i] = iv;
            }
            paintStars(stars, rating.get(), -1);

            Rectangle hit = new Rectangle(totalStarsW, STAR_SIZE + 8, Color.TRANSPARENT);
            hit.setTranslateX(starsX);
            hit.setTranslateY(starsY - 4);
            hit.setCursor(javafx.scene.Cursor.HAND);
            hit.setOnMouseMoved(e -> {
                int hover = Math.clamp((int) (e.getX() / (STAR_SIZE + STAR_GAP)) + 1, 1, 5);
                paintStars(stars, rating.get(), hover);
            });
            hit.setOnMouseExited(e -> paintStars(stars, rating.get(), -1));
            hit.setOnMouseClicked(e -> {
                int clicked = Math.clamp((int) (e.getX() / (STAR_SIZE + STAR_GAP)) + 1, 1, 5);
                rating.set(clicked);
                paintStars(stars, clicked, -1);
                hideThanksIfVisible(thanksH, submitH);
            });
            scene.addUINode(hit);
            iy += starsH + 10;

            // Comment textarea (.rv-comment)
            TextArea comment = new TextArea(existing != null && existing.getComment() != null ? existing.getComment() : "");
            comment.setPromptText("comment (optional)");
            comment.setWrapText(true);
            comment.setPrefWidth(cardW - cardPadX * 2);
            comment.setPrefHeight(commentH_);
            comment.setMaxSize(cardW - cardPadX * 2, commentH_);
            stylePixelTextArea(comment);
            comment.setTranslateX(cardX + cardPadX);
            comment.setTranslateY(iy);
            comment.textProperty().addListener((obs, was, now) -> {
                if (now.length() > COMMENT_MAX) comment.setText(was);
                else hideThanksIfVisible(thanksH, submitH);
            });
            comment.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    comment.getParent().requestFocus();
                    e.consume();
                }
            });
            scene.addUINode(comment);
            commentH[0] = comment;
            iy += commentH_ + 10;

            // Submit row / thanks (fixed height 24px to avoid layout shift)
            double rowY = iy;
            Text submit = FxglUi.createText(existing != null ? "UPDATE" : "SUBMIT", 10, FxglUi.DEFAULT_BUTTON_COLOR);
            submit.setTranslateX(cardX + (cardW - submit.getLayoutBounds().getWidth()) / 2.0);
            submit.setTranslateY(rowY + submitH_ / 2.0 - submit.getLayoutBounds().getMinY() - submit.getLayoutBounds().getHeight() / 2.0);
            FxglUi.wireMenuButton(submit, FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DEFAULT_ACTIVATION_COLOR);
            submit.setOnMouseClicked(e -> submitReview(currentUser, rating, commentH, submitH, thanksH, overallH));
            scene.addUINode(submit);
            submitH[0] = submit;

            Text thanks = FxglUi.createText("Thank you for your feedback!", 9, Color.rgb(145, 205, 255, 0.95));
            thanks.setTranslateX(cardX + (cardW - thanks.getLayoutBounds().getWidth()) / 2.0);
            thanks.setTranslateY(rowY + submitH_ / 2.0 - thanks.getLayoutBounds().getMinY() - thanks.getLayoutBounds().getHeight() / 2.0);
            thanks.setVisible(false);
            scene.addUINode(thanks);
            thanksH[0] = thanks;

            // ALL REVIEWS + BACK below card (menu-btn)
            double btnY = cardY + cardH + 24;
            Text allBtn = FxglUi.createText("ALL REVIEWS", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            allBtn.setTranslateY(btnY - allBtn.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, allBtn);
            btnY += allBtn.getLayoutBounds().getHeight() + 15;

            Text backBtn = FxglUi.createText("BACK", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            backBtn.setTranslateY(btnY - backBtn.getLayoutBounds().getMinY());
            FxglUi.addTextCenteredX(scene, backBtn);

            FxglUi.wireMenuButton(allBtn,  FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DEFAULT_ACTIVATION_COLOR);
            FxglUi.wireMenuButton(backBtn, FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DEFAULT_ACTIVATION_COLOR);
            allBtn.setOnMouseClicked(e -> showAllReviewsModal());
            backBtn.setOnMouseClicked(e -> input.push(InputType.QUIT));

            latch.countDown();
        });
        await(latch);

        // Main keyboard loop: LEFT/RIGHT pick rating, ENTER submits, ESC quits.
        while (true) {
            InputType in = input.getInput();
            switch (in) {
                case LEFT -> {
                    int r = Math.max(0, rating.get() - 1);
                    rating.set(r);
                    Platform.runLater(() -> paintStars(stars, r, -1));
                    hideThanksIfVisible(thanksH, submitH);
                }
                case RIGHT -> {
                    int r = Math.min(5, rating.get() + 1);
                    rating.set(r);
                    Platform.runLater(() -> paintStars(stars, r, -1));
                    hideThanksIfVisible(thanksH, submitH);
                }
                case ENTER -> submitReview(currentUser, rating, commentH, submitH, thanksH, overallH);
                case QUIT  -> { return; }
                default    -> {}
            }
        }
    }

    private void submitReview(User currentUser, AtomicInteger rating, TextArea[] commentH,
                               Text[] submitH, Text[] thanksH, Text[] overallH) {
        if (rating.get() == 0) return;
        String comment = commentH[0] != null ? commentH[0].getText() : "";
        reviewService.addOrUpdateReview(new Review(currentUser.getId(), rating.get(), comment));
        Float overall = reviewService.getOverallRating();
        Platform.runLater(() -> {
            submitH[0].setVisible(false);
            thanksH[0].setVisible(true);
            submitH[0].setText("UPDATE");
            overallH[0].setText(overallText(overall));
            overallH[0].setTranslateX((FXGL.getAppWidth() - overallH[0].getLayoutBounds().getWidth()) / 2.0);
        });
    }

    private void hideThanksIfVisible(Text[] thanksH, Text[] submitH) {
        if (thanksH[0] != null && thanksH[0].isVisible()) {
            Platform.runLater(() -> {
                thanksH[0].setVisible(false);
                submitH[0].setVisible(true);
            });
        }
    }

    // ─── Titles + card helpers ─────────────────────────────────────────────

    private void buildTitles(GameScene scene) {
        // .menu-wrap margin-top: -140 → titles centered around (appH/2 - 70)
        // RATE / THE GAME at 56px (review.html overrides to 56px)
        double appH = FXGL.getAppHeight();
        Text t1 = FxglUi.createText("RATE", 56, FxglUi.DEFAULT_TITLE_COLOR);
        Text t2 = FxglUi.createText("THE GAME", 56, FxglUi.DEFAULT_TITLE_COLOR);
        double h1 = t1.getLayoutBounds().getHeight();
        double h2 = t2.getLayoutBounds().getHeight();
        double y = (appH / 2.0) - 140 + 0; // approximate
        // Keep a sensible absolute top so cards fit
        y = 40;
        t1.setTranslateY(y - t1.getLayoutBounds().getMinY());
        FxglUi.addTextCenteredX(scene, t1, -8);
        y += h1 + 8;
        t2.setTranslateY(y - t2.getLayoutBounds().getMinY());
        FxglUi.addTextCenteredX(scene, t2, 8);
    }

    private static String overallText(Float overall) {
        return (overall != null && overall > 0)
                ? String.format(Locale.US, "%.2f / 5", overall)
                : "no ratings yet";
    }

    /** Dark translucent bg + gold 2px border (matches .rv-comment in review.css). */
    private static void stylePixelTextArea(TextArea ta) {
        ta.setStyle(
            "-fx-control-inner-background: rgba(0,0,0,0.5);" +
            "-fx-background-color: rgba(0,0,0,0.5);" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Press Start 2P';" +
            "-fx-font-size: 9px;" +
            "-fx-border-color: rgba(245,197,24,0.35);" +
            "-fx-border-width: 2;" +
            "-fx-padding: 0;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
    }

    // ─── Star painting ─────────────────────────────────────────────────────

    private void paintStars(ImageView[] stars, int selected, int hover) {
        for (int i = 0; i < stars.length; i++) {
            int n = i + 1;
            boolean filled;
            double opacity;
            if (hover > 0) {
                filled = false;
                opacity = n <= hover ? STAR_OPACITY_PREVIEW : STAR_OPACITY_EMPTY;
            }
            else if (n <= selected) {
                filled = true;
                opacity = STAR_OPACITY_FILLED;
            }
            else {
                filled = false;
                opacity = STAR_OPACITY_EMPTY;
            }
            stars[i].setOpacity(opacity);
            stars[i].setEffect(filled ? starGlow() : null);
        }
    }

    private static DropShadow starGlow() {
        DropShadow ds = new DropShadow();
        ds.setColor(STAR_GLOW);
        ds.setRadius(4);
        ds.setSpread(0.3);
        return ds;
    }

    // ─── All reviews modal (brown-gold .rv-modal-box) ──────────────────────

    private void showAllReviewsModal() {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(1);
        FxglUi.Modal[] modalHolder = new FxglUi.Modal[1];
        VBox[] listBoxHolder = new VBox[1];

        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            double W = FXGL.getAppWidth(), H = FXGL.getAppHeight();
            double panelW = 460, panelH = 440;

            Group content = new Group();
            content.setTranslateX(W / 2.0);
            content.setTranslateY(H / 2.0);

            Rectangle panel = new Rectangle(panelW, panelH);
            panel.setFill(FxglUi.MODAL_PANEL_FILL);
            panel.setStroke(FxglUi.MODAL_PANEL_STROKE);
            panel.setStrokeWidth(FxglUi.MODAL_PANEL_STROKE_WIDTH);
            panel.setArcWidth(FxglUi.MODAL_PANEL_ARC);
            panel.setArcHeight(FxglUi.MODAL_PANEL_ARC);
            panel.setTranslateX(-panelW / 2.0);
            panel.setTranslateY(-panelH / 2.0);
            content.getChildren().add(panel);

            Text titleNode = FxglUi.createText("ALL REVIEWS", 14, FxglUi.DEFAULT_TITLE_COLOR);
            titleNode.setTranslateX(-titleNode.getLayoutBounds().getWidth() / 2.0);
            titleNode.setTranslateY(-panelH / 2.0 + 32 - titleNode.getLayoutBounds().getMinY());
            content.getChildren().add(titleNode);

            // Placeholder list — populated once the background fetch completes.
            VBox listBox = new VBox(10);
            listBox.setPadding(new Insets(0, 4, 0, 0));
            listBox.setFillWidth(true);
            listBox.setStyle("-fx-background-color: transparent;");
            Text loadingText = FxglUi.createText("Loading...", 9, Color.rgb(255, 255, 255, 0.4));
            listBox.getChildren().add(loadingText);
            listBoxHolder[0] = listBox;

            ScrollPane scroll = new ScrollPane(listBox);
            scroll.setFitToWidth(true);
            scroll.setPrefWidth(panelW - 64);
            scroll.setPrefHeight(panelH - 120);
            scroll.setTranslateX(-panelW / 2.0 + 32);
            scroll.setTranslateY(-panelH / 2.0 + 64);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setStyle(
                "-fx-background: transparent;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;" +
                "-fx-focus-color: transparent;"
            );
            content.getChildren().add(scroll);

            // CLOSE button (nav-btn style at bottom)
            Text closeBtn = FxglUi.createText("CLOSE", 10, FxglUi.DEFAULT_BUTTON_COLOR);
            closeBtn.setTranslateX(-closeBtn.getLayoutBounds().getWidth() / 2.0);
            closeBtn.setTranslateY(panelH / 2.0 - 24);
            closeBtn.setOnMouseClicked(e -> closeLatch.countDown());
            FxglUi.wireMenuButton(closeBtn, FxglUi.DEFAULT_BUTTON_COLOR, FxglUi.DEFAULT_ACTIVATION_COLOR);
            content.getChildren().add(closeBtn);

            modalHolder[0] = FxglUi.openModal(scene, FxglUi.MODAL_OVERLAY_ALPHA, content);
            openLatch.countDown();
        });
        await(openLatch);

        // Fetch reviews on a background thread so the modal opens immediately
        // with "Loading..." and doesn't block the JavaFX thread on network I/O.
        Thread fetch = new Thread(() -> {
            List<Review> reviews;
            try { reviews = reviewService.getAllReviews(); }
            catch (Exception ex) { reviews = null; }

            // Names are also fetched here to keep JavaFX thread free.
            java.util.List<ReviewRow> rows = new java.util.ArrayList<>();
            if (reviews != null) {
                for (Review r : reviews) {
                    String name;
                    try { name = userService.getUserNameById(r.getUserId()); }
                    catch (Exception ex) { name = null; }
                    rows.add(new ReviewRow(name != null ? name : "Unknown", r));
                }
            }
            final java.util.List<ReviewRow> finalRows = rows;
            final boolean hadData = reviews != null;

            Platform.runLater(() -> {
                VBox listBox = listBoxHolder[0];
                if (listBox == null) return;
                listBox.getChildren().clear();
                if (!hadData || finalRows.isEmpty()) {
                    Text empty = FxglUi.createText(
                            hadData ? "No reviews yet" : "Could not load reviews",
                            9, Color.rgb(255, 255, 255, 0.4));
                    listBox.getChildren().add(empty);
                }
                else {
                    for (ReviewRow row : finalRows) {
                        listBox.getChildren().add(buildReviewItem(row.name(), row.review()));
                    }
                }
            });
        }, "rv-fetch");
        fetch.setDaemon(true);
        fetch.start();

        Thread kbd = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                InputType in = input.getInput();
                if (in == InputType.ENTER || in == InputType.QUIT) {
                    closeLatch.countDown();
                    return;
                }
            }
        }, "rv-modal-kbd");
        kbd.setDaemon(true);
        kbd.start();

        await(closeLatch);
        kbd.interrupt();

        CountDownLatch closed = new CountDownLatch(1);
        Platform.runLater(() -> modalHolder[0].close(closed::countDown));
        await(closed);
    }

    private record ReviewRow(String name, Review review) {}

    private VBox buildReviewItem(String userName, Review r) {
        VBox item = new VBox(4);
        item.setPadding(new Insets(0, 0, 10, 0));
        item.setStyle("-fx-border-color: rgba(245, 197, 24, 0.15); -fx-border-width: 0 0 1 0;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text name = FxglUi.createText(userName, 9, FxglUi.DEFAULT_TITLE_COLOR);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        HBox stars = buildReviewStars(r.getRating(), 12);
        header.getChildren().addAll(name, spacer, stars);
        item.getChildren().add(header);

        if (r.getComment() != null && !r.getComment().isBlank()) {
            Text comment = FxglUi.createText(r.getComment(), 8, Color.rgb(255, 255, 255, 0.7));
            comment.setWrappingWidth(380);
            item.getChildren().add(comment);
        }

        if (r.getUpdatedAt() != null) {
            Text date = FxglUi.createText(r.getUpdatedAt().format(DATE_FMT), 7, Color.rgb(255, 255, 255, 0.25));
            item.getChildren().add(date);
        }

        return item;
    }

    private HBox buildReviewStars(int rating, int size) {
        HBox row = new HBox(2);
        for (int i = 0; i < 5; i++) {
            ImageView iv = new ImageView(starImage());
            iv.setSmooth(false);
            iv.setPreserveRatio(true);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setCache(true);
            iv.setCacheHint(javafx.scene.CacheHint.SPEED);
            iv.setOpacity(i < rating ? STAR_OPACITY_FILLED : STAR_OPACITY_EMPTY);
            if (i < rating) iv.setEffect(starGlow());
            row.getChildren().add(iv);
        }
        return row;
    }

    // ─── Utilities ─────────────────────────────────────────────────────────

    private static String safe(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private void await(CountDownLatch latch) {
        try { latch.await(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
