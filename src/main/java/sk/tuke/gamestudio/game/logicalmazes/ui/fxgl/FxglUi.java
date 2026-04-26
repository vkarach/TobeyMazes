package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;

import java.util.ArrayList;
import java.util.List;

public class FxglUi {
    public static final Color DEFAULT_BACKGROUND = Color.rgb(68, 35, 97);

    public static final Color DEFAULT_TEXT_COLOR    = Color.WHITE;
    public static final Color DEFAULT_TITLE_COLOR   = Color.rgb(245, 197, 24);   // #F5C518
    public static final Color DEFAULT_BUTTON_COLOR  = Color.rgb(145, 205, 255);  // #91CDFF
    public static final Color DEFAULT_ACTIVATION_COLOR = Color.rgb(255, 215, 0); // #FFD700
    public static final Color DIM_WHITE             = Color.rgb(255, 255, 255, 0.45);
    public static final Color DANGER_COLOR          = Color.rgb(255, 107, 107, 0.7);

    public static final int DEFAULT_PAD       = 30;
    public static final int TITLE_SIZE        = 72;
    public static final int SUB_TITLE_SIZE    = 56;
    public static final int BUTTON_SIZE       = 18;
    public static final int NAV_BTN_SIZE      = 10;
    public static final int DEFAULT_TEXT_SIZE = 18;

    // Snappy animation timings
    private static final Duration BTN_ANIM = Duration.millis(120);
    private static final Duration MODAL_OVERLAY = Duration.millis(100);
    private static final Duration MODAL_CONTENT = Duration.millis(120);
    private static final double BTN_HOVER_SHIFT = 4.0;
    private static final double MODAL_INIT_SCALE = 0.85;
    private static final double MODAL_INIT_Y_OFFSET = 12.0;

    // Shared modal (level modal, rv-modal-box, win-overlay) brown/gold look.
    public static final Color MODAL_PANEL_FILL   = Color.rgb(20, 10, 5, 0.92);
    public static final Color MODAL_PANEL_STROKE = Color.rgb(245, 197, 24, 0.5);
    public static final double MODAL_PANEL_STROKE_WIDTH = 2.0;
    public static final double MODAL_PANEL_ARC = 4.0;
    public static final double MODAL_OVERLAY_ALPHA = 0.75; // matches base.css .modal-overlay

    public static Font createFont(int size) {
        return Font.loadFont(
            FileReader.getInputStream("ui/shared/fonts/PressStart2P-Regular.ttf"),
            size
        );
    }

    public static Text createText(String text) {
        return createText(text, DEFAULT_TEXT_SIZE, 0, 0, DEFAULT_TEXT_COLOR);
    }

    public static Text createText(String text, Color color) {
        return createText(text, DEFAULT_TEXT_SIZE, 0, 0, color);
    }

    public static Text createText(String text, int size, Color color) {
        return createText(text, size, 0, 0, color);
    }

    public static Text createText(String text, int size, int translateX, int translateY) {
        return createText(text, size, translateX, translateY, Color.WHITE);
    }

    public static Text createTitle(String text, Color color) {
        return createText(text, TITLE_SIZE, 0, 0, color);
    }

    public static Text createSubTitle(String text, Color color) {
        return createText(text, SUB_TITLE_SIZE, 0, 0, color);
    }

    /** Card panel background matching CSS .card-panel */
    public static Rectangle createCardPanel(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(w, h);
        r.setFill(Color.rgb(20, 10, 5, 0.82));
        r.setStroke(Color.rgb(245, 197, 24, 0.5));
        r.setStrokeWidth(2);
        r.setArcWidth(4);
        r.setArcHeight(4);
        r.setTranslateX(x);
        r.setTranslateY(y);
        return r;
    }

    /** Horizontal gradient separator matching CSS .gradient-sep */
    public static Rectangle createGradientSep(double x, double y, double w) {
        LinearGradient g = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.TRANSPARENT),
            new Stop(0.3, Color.rgb(245, 197, 24, 0.55)),
            new Stop(0.7, Color.rgb(245, 197, 24, 0.55)),
            new Stop(1.0, Color.TRANSPARENT)
        );
        Rectangle r = new Rectangle(w, 2);
        r.setFill(g);
        r.setTranslateX(x);
        r.setTranslateY(y);
        return r;
    }

    public static Text createText(String text, int size, int translateX, int translateY, Color color) {
        Font font = createFont(size);
        Text textObj = new Text(text);
        textObj.setFont(font);
        textObj.setFill(color);
        if (size > 14) {
            textObj.setStroke(Color.BLACK);
            textObj.setStrokeWidth(size > 30 ? 2.5 : 1.0);
        }

        int shadowOffset;
        if (size > 30) shadowOffset = 4;
        else if (size > 18) shadowOffset = 2;
        else if (size > 14) shadowOffset = 1;
        else shadowOffset = 0;
        textObj.setEffect(pixelShadow(shadowOffset));

        textObj.setTranslateX(translateX);
        textObj.setTranslateY(translateY);
        return textObj;
    }

    public static void clearContentNodes(GameScene scene, List<Node> toKeep) {
        List<Node> current = new ArrayList<>(scene.getUINodes());
        for (Node node : current) {
            if (!toKeep.contains(node)) {
                scene.removeUINode(node);
            }
        }
    }

    public static void addTextCenteredX(GameScene scene, Text text) {
        addTextCenteredX(scene, text, 0);
    }

    public static void addTextCenteredX(GameScene scene, Text text, int shift) {
        scene.addUINode(text);
        text.setTranslateX((FXGL.getAppWidth() - text.getLayoutBounds().getWidth()) / 2.0 + shift);
    }

    private static DropShadow pixelShadow(int offset) {
        if (offset == 0) return null;
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        ds.setRadius(0);
        ds.setSpread(1);
        ds.setOffsetX(offset);
        ds.setOffsetY(offset);
        return ds;
    }

    // ─── Button wiring (web .menu-btn parity) ──────────────────────────────

    /**
     * Hover = 150ms color fade normal→hover + translateX +4px. Exit reverses.
     * Safe to call after the button's translateX is set; captures base X.
     */
    public static void wireMenuButton(Text btn, Color normal, Color hover) {
        btn.setCursor(Cursor.HAND);
        btn.setFill(normal);
        final double baseX = btn.getTranslateX();
        btn.setOnMouseEntered(e -> {
            animateFill(btn, hover, BTN_ANIM);
            animateTranslateX(btn, baseX + BTN_HOVER_SHIFT, BTN_ANIM);
        });
        btn.setOnMouseExited(e -> {
            animateFill(btn, normal, BTN_ANIM);
            animateTranslateX(btn, baseX, BTN_ANIM);
        });
    }

    /** Color-only hover (no translate) — for dim links like "&lt; Back to Menu". */
    public static void wireLinkHover(Text btn, Color normal, Color hover) {
        btn.setCursor(Cursor.HAND);
        btn.setFill(normal);
        btn.setOnMouseEntered(e -> animateFill(btn, hover, BTN_ANIM));
        btn.setOnMouseExited(e -> animateFill(btn, normal, BTN_ANIM));
    }

    public static void animateFill(Shape target, Color to, Duration dur) {
        Color from = (target.getFill() instanceof Color c) ? c : Color.WHITE;
        if (from.equals(to)) return;
        FillTransition ft = new FillTransition(dur, target, from, to);
        ft.setInterpolator(Interpolator.EASE_BOTH);
        ft.play();
    }

    public static void animateTranslateX(Node target, double toX, Duration dur) {
        if (target.getTranslateX() == toX) return;
        TranslateTransition tt = new TranslateTransition(dur, target);
        tt.setToX(toX);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    // ─── ScrollPane — gold thumb matching base.css ─────────────────────────

    /**
     * Transparent background + thin 6px scroll-bar with rgba(245,197,24,0.3)
     * thumb — matches the {@code scrollbar-thumb} styling in static/css/base.css.
     */
    public static void styleTransparentScrollPane(javafx.scene.control.ScrollPane scroll) {
        scroll.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        applyGoldScrollBar(scroll);
    }

    private static final String GOLD_SCROLL_BAR_CSS =
        ".scroll-bar:horizontal, .scroll-bar:vertical { -fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; }" +
        ".scroll-bar:vertical { -fx-pref-width: 6px; }" +
        ".scroll-bar:horizontal { -fx-pref-height: 6px; }" +
        ".scroll-bar .track { -fx-background-color: transparent; -fx-border-color: transparent; }" +
        ".scroll-bar .track-background { -fx-background-color: transparent; }" +
        ".scroll-bar .thumb { -fx-background-color: rgba(245,197,24,0.3); -fx-background-insets: 0; -fx-background-radius: 3px; }" +
        ".scroll-bar .thumb:hover, .scroll-bar .thumb:pressed { -fx-background-color: rgba(245,197,24,0.5); }" +
        ".scroll-bar .increment-button, .scroll-bar .decrement-button { -fx-background-color: transparent; -fx-padding: 0; }" +
        ".scroll-bar .increment-arrow, .scroll-bar .decrement-arrow { -fx-shape: null; -fx-padding: 0; }" +
        ".scroll-pane > .corner { -fx-background-color: transparent; }" +
        ".scroll-pane > .viewport { -fx-background-color: transparent; }";

    private static void applyGoldScrollBar(javafx.scene.Parent target) {
        try {
            String dataUri = "data:," + java.net.URLEncoder.encode(
                    GOLD_SCROLL_BAR_CSS, java.nio.charset.StandardCharsets.UTF_8);
            target.getStylesheets().add(dataUri);
        }
        catch (Exception ignored) {}
    }

    // ─── TextField pixel-art style ─────────────────────────────────────────

    public static void stylePixelTextField(TextField tf) {
        tf.setFont(createFont(11));
        tf.setStyle(
            "-fx-background-color: rgba(0,0,0,0.75);" +
            "-fx-text-fill: white;" +
            "-fx-border-color: rgb(145,205,255);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8;"
        );
    }

    // ─── Modal open/close ──────────────────────────────────────────────────

    /**
     * Holds a modal's overlay + content group and provides animated close().
     * Content is wrapped in a Group which is scaled/translated as one unit.
     */
    public static final class Modal {
        private final GameScene scene;
        private final Rectangle overlay;
        private final Group content;
        private final double overlayAlpha;
        private boolean closing = false;

        Modal(GameScene scene, Rectangle overlay, Group content, double overlayAlpha) {
            this.scene = scene;
            this.overlay = overlay;
            this.content = content;
            this.overlayAlpha = overlayAlpha;
        }

        public void close(Runnable afterClose) {
            if (closing) return;
            closing = true;

            FadeTransition overlayOut = new FadeTransition(MODAL_OVERLAY, overlay);
            overlayOut.setFromValue(overlayAlpha);
            overlayOut.setToValue(0.0);

            FadeTransition contentFadeOut = new FadeTransition(MODAL_CONTENT, content);
            contentFadeOut.setFromValue(1.0);
            contentFadeOut.setToValue(0.0);

            ScaleTransition contentScaleOut = new ScaleTransition(MODAL_CONTENT, content);
            contentScaleOut.setToX(MODAL_INIT_SCALE);
            contentScaleOut.setToY(MODAL_INIT_SCALE);

            ParallelTransition out = new ParallelTransition(
                    overlayOut, contentFadeOut, contentScaleOut);
            out.setOnFinished(e -> {
                scene.removeUINode(overlay);
                scene.removeUINode(content);
                if (afterClose != null) afterClose.run();
            });
            out.play();
        }
    }

    /**
     * Pop up a modal with animated entry: overlay fade 0→alpha (250ms) and
     * content fade 0→1 + scale 0.70→1.0 (300ms). Populate {@code content}
     * (Group) before calling — its children are already in final positions
     * because the Group's transforms are what animate.
     */
    public static Modal openModal(GameScene scene, double overlayAlpha, Group content) {
        double W = FXGL.getAppWidth(), H = FXGL.getAppHeight();
        Rectangle overlay = new Rectangle(W, H, Color.rgb(0, 0, 0, overlayAlpha));
        overlay.setOpacity(0);
        content.setOpacity(0);
        content.setScaleX(MODAL_INIT_SCALE);
        content.setScaleY(MODAL_INIT_SCALE);

        scene.addUINode(overlay);
        scene.addUINode(content);

        FadeTransition overlayIn = new FadeTransition(MODAL_OVERLAY, overlay);
        overlayIn.setFromValue(0.0);
        overlayIn.setToValue(1.0);

        FadeTransition contentFade = new FadeTransition(MODAL_CONTENT, content);
        contentFade.setFromValue(0.0);
        contentFade.setToValue(1.0);

        ScaleTransition contentScale = new ScaleTransition(MODAL_CONTENT, content);
        contentScale.setToX(1.0);
        contentScale.setToY(1.0);
        contentScale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(overlayIn, contentFade, contentScale).play();

        return new Modal(scene, overlay, content, overlayAlpha);
    }
}
