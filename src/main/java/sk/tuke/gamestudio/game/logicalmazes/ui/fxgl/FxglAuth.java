package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.ui.AuthView;
import sk.tuke.gamestudio.service.AuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Profile("fxgl")
@Component
public class FxglAuth implements AuthView {

    private static final String REGEX_NAME  = "^[A-Za-z0-9_-]+$";
    private static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
    private static final String REGEX_CODE  = "^\\d{6}$";

    private final AuthService authService;

    public FxglAuth(AuthService authService) {
        this.authService = authService;
    }

    // ── Public API ──────────────────────────────────────────────────────────────

    @Override
    public User register() {
        String nameError = null;
        while (true) {
            String name = askValidated("ENTER USERNAME", false,
                    v -> validate(v, REGEX_NAME, 3, 16), nameError);
            nameError = null;
            if (name == null) return null;

            Runnable stopCheck = showLoading("CHECKING");
            boolean nameTaken = authService.userNameTaken(name);
            stopCheck.run();

            if (nameTaken) {
                nameError = "Username already taken";
                continue;
            }

            String password = askValidated("ENTER PASSWORD", true,
                    v -> validate(v, REGEX_NAME, 3, 16), null);
            if (password == null) continue; // ESC - back to username

            String email = null;
            String emailError = null;
            while (email == null) {
                String input = askValidated("ENTER EMAIL", false,
                        v -> validate(v, REGEX_EMAIL, 5, 60), emailError);
                if (input == null) break; // ESC - back to username loop

                Runnable stopEmailCheck = showLoading("CHECKING");
                boolean emailTaken = authService.emailTaken(input);
                stopEmailCheck.run();

                if (emailTaken) { emailError = "Email already registered"; continue; }
                email = input;
            }
            if (email == null) continue; // ESC from email → back to username

            Runnable stopSend = showLoading("SENDING CODE");
            int code = authService.sendOrGetVerificationCodeByEmail(email);
            stopSend.run();

            if (!askCode(code, email)) continue; // wrong code or ESC → restart

            Runnable stopReg = showLoading("REGISTERING");
            User user = authService.register(name, password, email);
            authService.expireEmail(email);
            stopReg.run();

            return user;
        }
    }

    @Override
    public User login() {
        while (true) {
            String name = askValidated("ENTER USERNAME", false,
                    v -> validate(v, REGEX_NAME, 3, 16), null);
            if (name == null) return null;

            String password = askValidated("ENTER PASSWORD", true,
                    v -> validate(v, REGEX_NAME, 3, 16), null);
            if (password == null) continue;

            Runnable stop = showLoading("LOGGING IN");
            User user = authService.login(name, password);
            stop.run();

            if (user == null) {
                // Show error on the username field and loop back
                String retryName = askValidated("ENTER USERNAME", false,
                        v -> validate(v, REGEX_NAME, 3, 16), "Wrong name or password");
                if (retryName == null) return null;
                // Re-enter username as next iteration
                continue;
            }
            return user;
        }
    }

    @Override
    public void changePassword(int userId) {
        Runnable stop = showLoading("SENDING CODE");
        int code = authService.getOrCreateEmailVerificationCode(userId);
        stop.run();

        if (!askCode(code, null)) return;

        String newPassword = askValidated("NEW PASSWORD", true,
                v -> validate(v, REGEX_NAME, 3, 16), null);
        if (newPassword == null) return;

        Runnable stopChange = showLoading("SAVING");
        authService.changePassword(userId, newPassword);
        authService.expireEmailByUserId(userId);
        stopChange.run();
    }

    // ── Validation helpers ──────────────────────────────────────────────────────

    private static String validate(String input, String regex, int min, int max) {
        if (input.length() < min) return "Min " + min + " characters";
        if (input.length() > max) return "Max " + max + " characters";
        if (!input.matches(regex)) return "Invalid characters";
        return null;
    }

    // ── High-level UI helpers ───────────────────────────────────────────────────

    /** Loops askField until validator returns null (valid) or ESC is pressed (returns null). */
    private String askValidated(String label, boolean isPassword,
                                 Function<String, String> validator, String initialError) {
        String error = initialError;
        while (true) {
            String value = askField(label, null, isPassword, error);
            if (value == null) return null; // ESC
            error = validator.apply(value);
            if (error == null) return value;
        }
    }

    /**
     * Shows a panel with a code entry field.
     * Retries until correct code entered or ESC pressed.
     */
    private boolean askCode(int expectedCode, String email) {
        String subtitle = email != null ? "Code sent to " + email : "Code sent to your email";
        String error = null;
        while (true) {
            String input = askField("ENTER CODE", subtitle, false, error);
            if (input == null) return false; // ESC
            if (!input.matches(REGEX_CODE)) {
                error = "Must be exactly 6 digits";
                continue;
            }
            if (Integer.parseInt(input) != expectedCode) {
                error = "Wrong code";
                continue;
            }
            return true;
        }
    }

    /**
     * Shows loading animation panel on the FX thread.
     * Returns a Runnable that removes it (must be called from the game thread).
     */
    private Runnable showLoading(String message) {
        List<Node> nodes = new ArrayList<>();
        Timeline[] tl = {null};
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            double W = FXGL.getAppWidth(), H = FXGL.getAppHeight();
            double panelW = 440, panelH = 90;
            double panelX = (W - panelW) / 2, panelY = (H - panelH) / 2;

            Rectangle panel = new Rectangle(panelW, panelH);
            panel.setFill(Color.rgb(15, 5, 30, 0.96));
            panel.setStroke(FxglUi.DEFAULT_BUTTON_COLOR);
            panel.setStrokeWidth(2);
            panel.setArcWidth(12);
            panel.setArcHeight(12);
            panel.setTranslateX(panelX);
            panel.setTranslateY(panelY);

            Text text = FxglUi.createText(message, 15, Color.WHITE);
            text.setTranslateX(panelX + 24);
            text.setTranslateY(panelY + 56);

            String[] frames = {".", "..", "...", " "};
            int[] frame = {frames.length - 1};
            tl[0] = new Timeline(new KeyFrame(Duration.millis(150), e -> {
                frame[0] = (frame[0] + 1) % frames.length;
                text.setText(message + frames[frame[0]]);
            }));
            tl[0].setCycleCount(Timeline.INDEFINITE);
            tl[0].play();

            nodes.add(panel);
            nodes.add(text);
            nodes.forEach(FXGL.getGameScene()::addUINode);
            latch.countDown();
        });

        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        return () -> Platform.runLater(() -> {
            if (tl[0] != null) tl[0].stop();
            nodes.forEach(FXGL.getGameScene()::removeUINode);
        });
    }

    // ── Core field dialog ───────────────────────────────────────────────────────

    /**
     * Shows an input panel. Blocks game thread until Enter or ESC.
     *
     * Layout uses fixed offsets so the panel never resizes between calls
     * - error space is always reserved.
     *
     * @param subtitle optional second line (e.g. "Code sent to email")
     * @param error    optional red error text, auto-fades after 2.5 s
     * @return typed text, or null if ESC / ESC-BACK was pressed
     */
    private String askField(String title, String subtitle, boolean isPassword, String error) {
        CountDownLatch latch = new CountDownLatch(1);
        String[] result = {null};
        AtomicBoolean done = new AtomicBoolean(false);

        Platform.runLater(() -> {
            double W = FXGL.getAppWidth(), H = FXGL.getAppHeight();
            boolean hasSubtitle = subtitle != null && !subtitle.isEmpty();
            boolean hasError    = error    != null && !error.isEmpty();

            double pad    = 26;
            double panelW = isPassword ? 640 : 620;
            // Fixed height - always includes error row so panel never shifts on error
            double panelH = hasSubtitle ? 230 : 200;
            double panelX = (W - panelW) / 2;
            double panelY = (H - panelH) / 2;

            // Fixed Y offsets from panelY (Text Y = baseline; TextField Y = top-left)
            double titleY    = panelY + 42;
            double subtitleY = panelY + 76;
            double errorY    = panelY + (hasSubtitle ? 110 : 96);
            double fieldTopY = panelY + (hasSubtitle ? 130 : 116);
            double fieldW    = isPassword ? panelW - pad * 2 - 90 : panelW - pad * 2;
            double escY      = panelY + panelH - 20;

            List<Node> nodes = new ArrayList<>();
            Runnable cleanup = () -> nodes.forEach(FXGL.getGameScene()::removeUINode);
            Runnable[] cancelRef = {null};
            Runnable cancel = () -> {
                if (!done.compareAndSet(false, true)) return;
                cleanup.run();
                latch.countDown();
            };
            cancelRef[0] = cancel;

            // Panel background
            Rectangle panel = new Rectangle(panelW, panelH);
            panel.setFill(Color.rgb(15, 5, 30, 0.94));
            panel.setStroke(FxglUi.DEFAULT_TITLE_COLOR);
            panel.setStrokeWidth(2);
            panel.setArcWidth(14);
            panel.setArcHeight(14);
            panel.setTranslateX(panelX);
            panel.setTranslateY(panelY);

            Text titleNode = FxglUi.createText(title, 18, FxglUi.DEFAULT_TITLE_COLOR);
            titleNode.setTranslateX(panelX + pad);
            titleNode.setTranslateY(titleY);

            // ESC - BACK - clickable with hover highlight
            Color escNormal = Color.rgb(180, 180, 210);
            Color escHover  = FxglUi.DEFAULT_TITLE_COLOR;
            Text escHint = plainText("ESC - BACK", 13, escNormal);
            escHint.setTranslateX(panelX + pad);
            escHint.setTranslateY(escY);
            escHint.setCursor(Cursor.HAND);
            escHint.setOnMouseEntered(e -> escHint.setFill(escHover));
            escHint.setOnMouseExited(e -> escHint.setFill(escNormal));
            escHint.setOnMouseClicked(e -> cancelRef[0].run());

            nodes.add(panel);
            nodes.add(titleNode);
            if (hasSubtitle) {
                Text subtitleNode = plainText(subtitle, 13, Color.rgb(180, 200, 230));
                subtitleNode.setTranslateX(panelX + pad);
                subtitleNode.setTranslateY(subtitleY);
                nodes.add(subtitleNode);
            }
            nodes.add(escHint);

            // Error always in the reserved row - auto-fades after 2.5 s
            if (hasError) {
                Text errNode = plainText("!  " + error, 14, Color.rgb(255, 100, 100));
                errNode.setTranslateX(panelX + pad);
                errNode.setTranslateY(errorY);
                nodes.add(errNode);

                Timeline fade = new Timeline(
                    new KeyFrame(Duration.millis(2000), ev -> {}),
                    new KeyFrame(Duration.millis(2600),
                        new KeyValue(errNode.opacityProperty(), 0.0))
                );
                fade.play();
            }

            if (isPassword) {
                setupPasswordField(nodes, panelX, panelW, pad, fieldTopY, fieldW,
                        result, done, cleanup, cancel, latch);
            }
            else {
                TextField tf = new TextField();
                styleField(tf, panelX + pad, fieldTopY, fieldW);
                Runnable confirm = () -> {
                    if (!done.compareAndSet(false, true)) return;
                    result[0] = tf.getText();
                    cleanup.run();
                    latch.countDown();
                };
                tf.setOnAction(e -> confirm.run());
                tf.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) cancel.run(); });
                nodes.add(tf);
                nodes.forEach(FXGL.getGameScene()::addUINode);
                tf.requestFocus();
            }
        });

        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return result[0];
    }

    // ── Password field with manual * masking ────────────────────────────────────

    private void setupPasswordField(
            List<Node> nodes,
            double panelX, double panelW, double pad,
            double fieldY, double fieldW,
            String[] result, AtomicBoolean done,
            Runnable cleanup, Runnable cancel,
            CountDownLatch latch
    ) {
        String[] realPwd = {""};
        boolean[] showing = {false};

        TextField field = new TextField();
        styleField(field, panelX + pad, fieldY, fieldW);

        field.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (showing[0]) return;
            String ch = e.getCharacter();
            if (ch.isEmpty() || ch.charAt(0) < 32) return;
            int start = field.getSelection().getStart();
            int end   = field.getSelection().getEnd();
            realPwd[0] = realPwd[0].substring(0, start) + ch + realPwd[0].substring(end);
            field.setText("*".repeat(realPwd[0].length()));
            field.positionCaret(start + 1);
            e.consume();
        });

        field.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (showing[0]) return;
            int start = field.getSelection().getStart();
            int end   = field.getSelection().getEnd();
            switch (e.getCode()) {
                case BACK_SPACE -> {
                    if (start != end) {
                        realPwd[0] = realPwd[0].substring(0, start) + realPwd[0].substring(end);
                    } else if (start > 0) {
                        realPwd[0] = realPwd[0].substring(0, start - 1) + realPwd[0].substring(start);
                        start--;
                    }
                    field.setText("*".repeat(realPwd[0].length()));
                    field.positionCaret(start);
                    e.consume();
                }
                case DELETE -> {
                    if (start != end) {
                        realPwd[0] = realPwd[0].substring(0, start) + realPwd[0].substring(end);
                        field.setText("*".repeat(realPwd[0].length()));
                        field.positionCaret(start);
                    } else if (start < realPwd[0].length()) {
                        realPwd[0] = realPwd[0].substring(0, start) + realPwd[0].substring(start + 1);
                        field.setText("*".repeat(realPwd[0].length()));
                        field.positionCaret(start);
                    }
                    e.consume();
                }
                default -> {
                    if (e.isShortcutDown()) {
                        if (e.getCode() == KeyCode.V) {
                            String pasted = Clipboard.getSystemClipboard().getString();
                            if (pasted != null && !pasted.isEmpty()) {
                                realPwd[0] = realPwd[0].substring(0, start) + pasted + realPwd[0].substring(end);
                                field.setText("*".repeat(realPwd[0].length()));
                                field.positionCaret(start + pasted.length());
                            }
                            e.consume();
                        } else if (e.getCode() == KeyCode.C || e.getCode() == KeyCode.X) {
                            e.consume();
                        }
                    }
                }
            }
        });

        Runnable confirm = () -> {
            if (!done.compareAndSet(false, true)) return;
            result[0] = showing[0] ? field.getText() : realPwd[0];
            cleanup.run();
            latch.countDown();
        };
        field.setOnAction(e -> confirm.run());
        field.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) cancel.run(); });

        // SHOW / HIDE - plain text, no shadow, readable
        Text toggleBtn = plainText("SHOW", 14, FxglUi.DEFAULT_BUTTON_COLOR);
        toggleBtn.setTranslateX(panelX + panelW - pad - 60);
        toggleBtn.setTranslateY(fieldY + 22);
        toggleBtn.setCursor(Cursor.HAND);
        toggleBtn.setOnMouseClicked(e -> {
            showing[0] = !showing[0];
            if (showing[0]) {
                field.setText(realPwd[0]);
                toggleBtn.setText("HIDE");
            } else {
                realPwd[0] = field.getText();
                field.setText("*".repeat(realPwd[0].length()));
                toggleBtn.setText("SHOW");
            }
            field.positionCaret(field.getText().length());
            field.requestFocus();
        });

        nodes.addAll(List.of(field, toggleBtn));
        nodes.forEach(FXGL.getGameScene()::addUINode);
        field.requestFocus();
    }

    // ── Styling ─────────────────────────────────────────────────────────────────

    private void styleField(TextField field, double x, double y, double width) {
        field.setTranslateX(x);
        field.setTranslateY(y);
        field.setPrefWidth(width);
        field.setPrefHeight(36);
        field.setFont(FxglUi.createFont(14));
        field.setStyle(
            "-fx-background-color: rgba(0,0,0,0.75);" +
            "-fx-text-fill: white;" +
            "-fx-border-color: rgb(145,205,255);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 4 8 4 8;"
        );
    }

    /** Plain text without stroke/shadow - readable at small sizes. */
    private static Text plainText(String str, int size, Color color) {
        Text t = new Text(str);
        t.setFont(FxglUi.createFont(size));
        t.setFill(color);
        return t;
    }
}
