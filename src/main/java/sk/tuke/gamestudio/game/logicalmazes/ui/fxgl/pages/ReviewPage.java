package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglInput;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglUi;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.ParallaxBackground;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.Selector;
import sk.tuke.gamestudio.service.ReviewService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("fxgl")
@Component
public class ReviewPage {

    private static final Color STAR_FILLED  = Color.rgb(255, 220,  50);
    private static final Color STAR_PREVIEW = Color.rgb(255, 240, 150);
    private static final Color STAR_EMPTY   = Color.rgb(100, 100, 100);
    private static final int   STAR_SIZE      = 32;
    private static final int   STAR_GAP       = 54;
    private static final int   COMMENT_MAX_LEN = 30;
    private static final int   EXISTING_FONT_SIZE = 16;

    private final ReviewService reviewService;
    private final FxglInput input;
    private final Selector selector;
    private final ParallaxBackground bg;

    public ReviewPage(
            ReviewService reviewService,
            FxglInput input,
            Selector selector,
            @Qualifier("aboutBackground") ParallaxBackground bg
    ) {
        this.reviewService = reviewService;
        this.input = input;
        this.selector = selector;
        this.bg = bg;
    }

    public void show(User currentUser) {
        selector.ensureBindings();
        if (currentUser == null) {
            showGuest();
        } else {
            showForm(currentUser);
        }
    }

    private void showGuest() {
        Text[] backHolder = new Text[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            double y = buildTitle(scene);
            y = buildOverallRating(scene, new Text[1], y);

            Text msg = FxglUi.createText("Sign in to rate the game", FxglUi.DEFAULT_TEXT_COLOR);
            msg.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, msg);
            y += FxglUi.DEFAULT_PAD * 2;

            Text back = FxglUi.createText("Back", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
            back.setTranslateY(y);
            FxglUi.addTextCenteredX(scene, back);
            backHolder[0] = back;
            latch.countDown();
        });
        awaitLatch(latch);
        selector.waitForConfirm(backHolder[0], FxglUi.DEFAULT_ACTIVATION_COLOR);
    }

    private void showForm(User currentUser) {
        Review existing      = reviewService.getReview(currentUser.getId());
        AtomicInteger rating   = new AtomicInteger(existing != null ? existing.getRating() : 0);
        AtomicBoolean tfFocused  = new AtomicBoolean(false);
        AtomicBoolean submitFlag = new AtomicBoolean(false);
        // false = submit button visible, true = thanks visible (submitted, no changes yet)
        AtomicBoolean submitHidden = new AtomicBoolean(false);

        Text[]     stars        = new Text[5];
        TextField[] tfHolder    = new TextField[1];
        Text[]     submitHolder = new Text[1];
        Text[]     backHolder   = new Text[1];
        Text[]     thanksHolder = new Text[1];
        Text[]     overallHolder = new Text[1];
        double[]   thankYouY   = new double[1];
        double[]   backOrigX   = new double[1];

        // Callback: called when user changes stars or comment after submitting
        Runnable onChanged = () -> {
            if (submitHidden.get()) {
                submitHidden.set(false);
                submitHolder[0].setVisible(true);
                backHolder[0].setTranslateX(backOrigX[0]);
                if (thanksHolder[0] != null) thanksHolder[0].setVisible(false);
            }
        };

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameScene scene = FXGL.getGameScene();
            FxglUi.clearContentNodes(scene, bg.getAllNodes());
            bg.start(scene);

            // Focus sink: invisible Pane used to programmatically remove TextField focus
            Pane focusSink = new Pane();
            focusSink.setFocusTraversable(true);
            focusSink.setPrefSize(1, 1);
            focusSink.setTranslateX(-200);
            scene.addUINode(focusSink);

            double y = buildTitle(scene);
            y = buildOverallRating(scene, overallHolder, y);
            if (existing != null) y = buildExistingInfo(scene, existing, y);
            y = buildStars(scene, stars, rating, focusSink, onChanged, y);
            y = buildCommentField(scene, tfHolder, existing, tfFocused, submitFlag, focusSink, onChanged, y);
            y = buildButtons(scene, submitHolder, backHolder, submitFlag, focusSink, y);
            backOrigX[0] = backHolder[0].getTranslateX();
            thankYouY[0] = y;
            latch.countDown();
        });
        awaitLatch(latch);

        // main interaction loop
        while (true) {
            InputType in = input.getInput();
            switch (in) {
                case LEFT -> {
                    if (!tfFocused.get()) {
                        int r = Math.max(0, rating.get() - 1);
                        rating.set(r);
                        Platform.runLater(() -> { paintStars(stars, r, -1); onChanged.run(); });
                    }
                }
                case RIGHT -> {
                    if (!tfFocused.get()) {
                        int r = Math.min(5, rating.get() + 1);
                        rating.set(r);
                        Platform.runLater(() -> { paintStars(stars, r, -1); onChanged.run(); });
                    }
                }
                case ENTER -> {
                    int r = rating.get();
                    if (r == 0) continue;
                    if ((submitFlag.get() || tfFocused.get()) && !submitHidden.get()) {
                        // Submit
                        submitFlag.set(false);
                        String comment = tfHolder[0].getText();
                        reviewService.addOrUpdateReview(new Review(currentUser.getId(), r, comment));
                        submitHidden.set(true);
                        Platform.runLater(() -> {
                            // Refresh overall rating
                            Float val = reviewService.getOverallRating();
                            String txt = "Overall: " + (val != null ? String.format("%.2f *", val) : "no ratings yet");
                            overallHolder[0].setText(txt);
                            overallHolder[0].setTranslateX(
                                (FXGL.getAppWidth() - overallHolder[0].getLayoutBounds().getWidth()) / 2.0);
                            // Show thanks, hide submit
                            showThanks(FXGL.getGameScene(), submitHolder[0], backHolder[0], thanksHolder, thankYouY[0]);
                        });
                    } else {
                        submitFlag.set(false);
                        if (submitHidden.get()) {
                            return;
                        }
                        if (!tfFocused.get()) {
                            // Stars selected via keyboard → jump to comment field
                            Platform.runLater(() -> tfHolder[0].requestFocus());
                        }
                    }
                }
                case QUIT -> { return; }
                default -> {}
            }
        }
    }

    private double buildTitle(GameScene scene) {
        Text t1 = FxglUi.createTitle("RATE", FxglUi.DEFAULT_TITLE_COLOR);
        double y = FxglUi.DEFAULT_PAD + t1.getLayoutBounds().getHeight();
        t1.setTranslateY(y);
        FxglUi.addTextCenteredX(scene, t1, -40);
        y *= 2;
        Text t2 = FxglUi.createTitle("THE GAME", FxglUi.DEFAULT_TITLE_COLOR);
        t2.setTranslateY(y);
        FxglUi.addTextCenteredX(scene, t2, +40);
        return y * 1.3;
    }

    private double buildOverallRating(GameScene scene, Text[] holder, double y) {
        Float val = reviewService.getOverallRating();
        String txt = "Overall: " + (val != null ? String.format("%.2f *", val) : "no ratings yet");
        Text node = FxglUi.createText(txt, FxglUi.DEFAULT_TEXT_COLOR);
        node.setTranslateY(y);
        FxglUi.addTextCenteredX(scene, node);
        holder[0] = node;
        return y + FxglUi.DEFAULT_PAD * 2;
    }

    private double buildExistingInfo(GameScene scene, Review r, double y) {
        String raw = r.getComment() != null ? r.getComment() : "";
        String comment = raw.isEmpty() ? "" :
                "  \"" + (raw.length() > COMMENT_MAX_LEN
                        ? raw.substring(0, COMMENT_MAX_LEN) + "..."
                        : raw) + "\"";
        Text node = FxglUi.createText(
                String.format("Your rating: %d*%s", r.getRating(), comment),
                EXISTING_FONT_SIZE,
                Color.rgb(180, 180, 180)
        );
        node.setTranslateY(y);
        FxglUi.addTextCenteredX(scene, node);
        return y + FxglUi.DEFAULT_PAD * 1.5;
    }

    private double buildStars(GameScene scene, Text[] stars, AtomicInteger rating,
                               Pane focusSink, Runnable onChanged, double y) {
        int init    = rating.get();
        double totalW = 5.0 * STAR_GAP;
        double startX = (FXGL.getAppWidth() - totalW) / 2.0;

        for (int i = 0; i < 5; i++) {
            Text star = new Text("★");
            star.setFont(Font.font("System", FontWeight.BOLD, STAR_SIZE));
            star.setFill(i < init ? STAR_FILLED : STAR_EMPTY);
            star.setTranslateX(startX + i * STAR_GAP);
            star.setTranslateY(y);
            star.setMouseTransparent(true);
            scene.addUINode(star);
            stars[i] = star;
        }

        Rectangle hitArea = new Rectangle(totalW, STAR_SIZE + 8, Color.rgb(0, 0, 0, 0.01));
        hitArea.setTranslateX(startX);
        hitArea.setTranslateY(y - STAR_SIZE);
        hitArea.setOnMouseMoved(e -> {
            int hover = Math.clamp((int) (e.getX() / STAR_GAP) + 1, 1, 5);
            paintStars(stars, rating.get(), hover);
        });
        hitArea.setOnMouseExited(e -> paintStars(stars, rating.get(), -1));
        hitArea.setOnMouseClicked(e -> {
            int clicked = Math.clamp((int) (e.getX() / STAR_GAP) + 1, 1, 5);
            rating.set(clicked);
            paintStars(stars, clicked, -1);
            focusSink.requestFocus();
            onChanged.run();
        });
        scene.addUINode(hitArea);

        return y + FxglUi.DEFAULT_PAD * 1.5;
    }

    private double buildCommentField(GameScene scene, TextField[] holder, Review existing,
                                     AtomicBoolean tfFocused, AtomicBoolean submitFlag, Pane focusSink,
                                     Runnable onChanged, double y) {
        Text label = FxglUi.createText("Comment (optional):", FxglUi.DEFAULT_TEXT_COLOR);
        label.setTranslateY(y);
        FxglUi.addTextCenteredX(scene, label);
        y += FxglUi.DEFAULT_PAD;

        String init = (existing != null && existing.getComment() != null) ? existing.getComment() : "";
        TextField tf = new TextField(init);
        tf.setPrefWidth(450);
        tf.setMaxWidth(450);
        tf.setStyle("-fx-background-color: #1a0a2e;"
                + "-fx-text-fill: white;"
                + "-fx-font-family: 'Press Start 2P';"
                + "-fx-font-size: 11px;"
                + "-fx-border-color: #91cdff;"
                + "-fx-border-width: 1px;"
                + "-fx-padding: 8px;");
        tf.setTranslateX((FXGL.getAppWidth() - 450) / 2.0);
        tf.setTranslateY(y);
        tf.setFocusTraversable(false);

        tf.focusedProperty().addListener((obs, was, now) -> tfFocused.set(now));
        tf.textProperty().addListener((obs, was, now) -> onChanged.run());

        tf.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                focusSink.requestFocus();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                submitFlag.set(true);
                focusSink.requestFocus();
                input.push(InputType.ENTER);
                e.consume();
            }
        });

        scene.addUINode(tf);
        holder[0] = tf;
        return y + 62;
    }

    private double buildButtons(GameScene scene, Text[] submitHolder, Text[] backHolder,
                                AtomicBoolean submitFlag, Pane focusSink, double y) {
        Text submit = FxglUi.createText("Submit", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
        submit.setTranslateY(y);
        FxglUi.addTextCenteredX(scene, submit, -60);
        submit.setOnMouseClicked(e -> {
            submitFlag.set(true);
            focusSink.requestFocus();
            input.push(InputType.ENTER);
        });
        submit.setOnMouseEntered(e -> submit.setFill(FxglUi.DEFAULT_ACTIVATION_COLOR));
        submit.setOnMouseExited(e -> submit.setFill(FxglUi.DEFAULT_BUTTON_COLOR));
        submitHolder[0] = submit;

        Text back = FxglUi.createText("Back", FxglUi.BUTTON_SIZE, FxglUi.DEFAULT_BUTTON_COLOR);
        back.setTranslateY(y);
        FxglUi.addTextCenteredX(scene, back, +60);
        back.setOnMouseClicked(e -> input.push(InputType.QUIT));
        back.setOnMouseEntered(e -> back.setFill(FxglUi.DEFAULT_ACTIVATION_COLOR));
        back.setOnMouseExited(e -> back.setFill(FxglUi.DEFAULT_BUTTON_COLOR));
        backHolder[0] = back;

        return y + FxglUi.DEFAULT_PAD * 2;
    }

    private void paintStars(Text[] stars, int selected, int hover) {
        for (int i = 0; i < stars.length; i++) {
            int n = i + 1;
            Color c;
            if (hover > 0) {
                c = n <= hover ? STAR_PREVIEW : STAR_EMPTY;
            } else {
                c = n <= selected ? STAR_FILLED : STAR_EMPTY;
            }
            stars[i].setFill(c);
        }
    }

    private void showThanks(GameScene scene, Text submitBtn, Text backBtn, Text[] thanksHolder, double y) {
        submitBtn.setVisible(false);
        backBtn.setTranslateX((FXGL.getAppWidth() - backBtn.getLayoutBounds().getWidth()) / 2.0);

        Text thanks = FxglUi.createText("Thank you for your feedback!", FxglUi.DEFAULT_ACTIVATION_COLOR);
        thanks.setTranslateY(y);
        FxglUi.addTextCenteredX(scene, thanks);
        thanksHolder[0] = thanks;
    }

    private void awaitLatch(CountDownLatch latch) {
        try { latch.await(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
