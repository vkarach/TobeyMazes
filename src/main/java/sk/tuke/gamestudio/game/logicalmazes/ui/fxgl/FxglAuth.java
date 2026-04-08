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
import java.util.function.Consumer;
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
            if (password == null) continue;

            String email = null;
            String emailError = null;
            while (email == null) {
                String input = askValidated("ENTER EMAIL", false,
                        v -> validate(v, REGEX_EMAIL, 5, 60), emailError);
                if (input == null) break;

                Runnable stopEmailCheck = showLoading("CHECKING");
                boolean emailTaken = authService.emailTaken(input);
                stopEmailCheck.run();

                if (emailTaken) { emailError = "Email already registered"; continue; }
                email = input;
            }
            if (email == null) continue;

            Runnable stopSend = showLoading("SENDING CODE");
            int code = authService.sendOrGetVerificationCodeByEmail(email);
            stopSend.run();

            if (!askCode(code, email)) continue;

            Runnable stopReg = showLoading("REGISTERING");
            User user = authService.register(name, password, email);
            authService.expireEmail(email);
            stopReg.run();

            return user;
        }
    }

    @Override
    public User login() {
        String errorMsg = null;
        while (true) {
            String name = askValidated("ENTER USERNAME", false,
                    v -> validate(v, REGEX_NAME, 3, 16), errorMsg);
            errorMsg = null;
            if (name == null) return null;

            String password = askValidated("ENTER PASSWORD", true,
                    v -> validate(v, REGEX_NAME, 3, 16), null);
            if (password == null) continue;

            Runnable stop = showLoading("LOGGING IN");
            User user = authService.login(name, password);
            stop.run();

            if (user == null) {
                errorMsg = "Wrong name or password";
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

    private static String validate(String input, String regex, int min, int max) {
        if (input.length() < min) return "Min " + min + " characters";
        if (input.length() > max) return "Max " + max + " characters";
        if (!input.matches(regex)) return "Invalid characters";
        return null;
    }

    // Simplified - popup handles validation internally, no outer loop needed
    private String askValidated(String label, boolean isPassword,
                                 Function<String, String> validator, String initialError) {
        return askField(label, null, isPassword, validator, initialError);
    }

    private boolean askCode(int expectedCode, String email) {
        String subtitle = email != null ? "Code sent to " + email : "Code sent to your email";
        String val = askField("ENTER CODE", subtitle, false, input -> {
            if (!input.matches(REGEX_CODE)) return "Must be exactly 6 digits";
            if (Integer.parseInt(input) != expectedCode) return "Wrong code";
            return null;
        }, null);
        return val != null;
    }

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

    // Popup stays open on error - no close/reopen jitter.
    // validator is called on submit; null = success, string = error to show inline.
    // initialError is shown immediately (e.g. server-side errors like "Username already taken").
    private String askField(String title, String subtitle, boolean isPassword,
                             Function<String, String> validator, String initialError) {
        CountDownLatch latch = new CountDownLatch(1);
        String[] result = {null};
        AtomicBoolean done = new AtomicBoolean(false);

        Platform.runLater(() -> {
            double W = FXGL.getAppWidth(), H = FXGL.getAppHeight();
            boolean hasSubtitle = subtitle != null && !subtitle.isEmpty();

            double pad    = 26;
            double panelW = isPassword ? 640 : 620;
            // Fixed height - error row space is always reserved
            double panelH = hasSubtitle ? 230 : 200;
            double panelX = (W - panelW) / 2;
            double panelY = (H - panelH) / 2;

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

            Color escNormal = Color.rgb(180, 180, 210);
            Color escHover  = FxglUi.DEFAULT_TITLE_COLOR;
            Text escHint = plainText("ESC - BACK", 13, escNormal);
            escHint.setTranslateX(panelX + pad);
            escHint.setTranslateY(escY);
            escHint.setCursor(Cursor.HAND);
            escHint.setOnMouseEntered(e -> escHint.setFill(escHover));
            escHint.setOnMouseExited(e -> escHint.setFill(escNormal));
            escHint.setOnMouseClicked(e -> cancelRef[0].run());

            // Error node always present - opacity 0 when no error, no layout shift
            Text errNode = plainText("", 14, Color.rgb(255, 100, 100));
            errNode.setTranslateX(panelX + pad);
            errNode.setTranslateY(errorY);
            errNode.setOpacity(0);
            Timeline[] currentFade = {null};

            Consumer<String> showError = msg -> {
                if (currentFade[0] != null) currentFade[0].stop();
                errNode.setText("!  " + msg);
                errNode.setOpacity(1.0);
                currentFade[0] = new Timeline(
                    new KeyFrame(Duration.millis(2500)),
                    new KeyFrame(Duration.millis(3100),
                        new KeyValue(errNode.opacityProperty(), 0.0))
                );
                currentFade[0].play();
            };

            nodes.add(panel);
            nodes.add(titleNode);
            if (hasSubtitle) {
                Text subtitleNode = plainText(subtitle, 13, Color.rgb(180, 200, 230));
                subtitleNode.setTranslateX(panelX + pad);
                subtitleNode.setTranslateY(subtitleY);
                nodes.add(subtitleNode);
            }
            nodes.add(escHint);
            nodes.add(errNode);

            if (isPassword) {
                setupPasswordField(nodes, panelX, panelW, pad, fieldTopY, fieldW,
                        result, done, cleanup, cancel, latch, validator, showError);
                if (initialError != null && !initialError.isEmpty()) {
                    showError.accept(initialError);
                }
            } else {
                TextField tf = new TextField();
                styleField(tf, panelX + pad, fieldTopY, fieldW);
                Runnable confirm = () -> {
                    String val = tf.getText();
                    if (val.isBlank()) { showError.accept("Please fill in the field"); return; }
                    if (validator != null) {
                        String err = validator.apply(val);
                        if (err != null) { showError.accept(err); return; }
                    }
                    if (!done.compareAndSet(false, true)) return;
                    result[0] = val;
                    cleanup.run();
                    latch.countDown();
                };
                tf.setOnAction(e -> confirm.run());
                tf.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) cancel.run(); });
                nodes.add(tf);
                nodes.forEach(FXGL.getGameScene()::addUINode);
                if (initialError != null && !initialError.isEmpty()) {
                    showError.accept(initialError);
                }
                tf.requestFocus();
            }
        });

        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return result[0];
    }

    private void setupPasswordField(
            List<Node> nodes,
            double panelX, double panelW, double pad,
            double fieldY, double fieldW,
            String[] result, AtomicBoolean done,
            Runnable cleanup, Runnable cancel,
            CountDownLatch latch,
            Function<String, String> validator,
            Consumer<String> showError
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
            String val = showing[0] ? field.getText() : realPwd[0];
            if (val.isBlank()) { showError.accept("Please fill in the field"); return; }
            if (validator != null) {
                String err = validator.apply(val);
                if (err != null) { showError.accept(err); return; }
            }
            if (!done.compareAndSet(false, true)) return;
            result[0] = val;
            cleanup.run();
            latch.countDown();
        };
        field.setOnAction(e -> confirm.run());
        field.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) cancel.run(); });

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

    private static Text plainText(String str, int size, Color color) {
        Text t = new Text(str);
        t.setFont(FxglUi.createFont(size));
        t.setFill(color);
        return t;
    }
}
