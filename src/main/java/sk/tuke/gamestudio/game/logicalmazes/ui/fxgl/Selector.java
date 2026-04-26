package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.UserAction;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("fxgl")
@Component
public class Selector {
    private static final Duration SELECT_ANIM = Duration.millis(150);
    private static final double BTN_HOVER_SHIFT = 4.0;
    private static final double INDICATOR_OFFSET = 28.0;

    private final FxglInput gameInput;
    private volatile boolean inputRegistered = false;

    public Selector(FxglInput gameInput) {
        this.gameInput = gameInput;
    }

    public <T> T select(T[] items, List<Text> buttons, Color selectColor) {
        return select(items, buttons, selectColor, null);
    }

    public <T> T select(T[] items, List<Text> buttons, Color selectColor, Text indicator) {
        ensureInputRegistered();

        AtomicInteger selected = new AtomicInteger(0);

        List<Paint> origColors = buttons.stream().map(Text::getFill).toList();
        List<Double> baseXs    = buttons.stream().map(Text::getTranslateX).toList();

        Platform.runLater(() -> {
            for (int i = 0; i < buttons.size(); i++) {
                final int index = i;
                buttons.get(i).setOnMouseClicked(e -> {
                    selected.set(index);
                    gameInput.push(InputType.ENTER);
                });
                buttons.get(i).setOnMouseEntered(e -> {
                    selected.set(index);
                    updateSelection(buttons, index, selectColor, origColors, baseXs, indicator);
                });
                buttons.get(i).setCursor(javafx.scene.Cursor.HAND);
            }
            updateSelection(buttons, 0, selectColor, origColors, baseXs, indicator);
        });

        while (true) {
            InputType input = gameInput.getInput();
            int sel = selected.get();
            switch (input) {
                case DOWN  -> sel = (sel + 1) % items.length;
                case UP    -> sel = (sel - 1 + items.length) % items.length;
                case ENTER -> { return items[selected.get()]; }
                case QUIT  -> { return null; }
                default    -> { continue; }
            }
            selected.set(sel);
            final int finalSel = sel;
            Platform.runLater(() -> updateSelection(buttons, finalSel, selectColor, origColors, baseXs, indicator));
        }
    }

    public void waitForConfirm(Text button, Color selectColor) {
        Paint origButtonColor = button.getFill();
        ensureInputRegistered();
        Platform.runLater(() -> {
            button.setOnMouseClicked(e -> gameInput.push(InputType.ENTER));
            FxglUi.wireMenuButton(button, (Color) origButtonColor, selectColor);
        });
        while (true) {
            InputType input = gameInput.getInput();
            if (input == InputType.ENTER || input == InputType.QUIT) {
                return;
            }
        }
    }

    public Thread onQuit(Runnable callback) {
        ensureInputRegistered();
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (gameInput.getInput() == InputType.QUIT) {
                    Platform.runLater(callback);
                    return;
                }
            }
        }, "quit-watcher");
        t.setDaemon(true);
        t.start();
        return t;
    }

    public void ensureBindings() {
        ensureInputRegistered();
    }

    private void ensureInputRegistered() {
        if (inputRegistered) return;
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            addBinding("NavUp",     KeyCode.UP,     InputType.UP);
            addBinding("NavDown",   KeyCode.DOWN,   InputType.DOWN);
            addBinding("NavLeft",   KeyCode.LEFT,   InputType.LEFT);
            addBinding("NavRight",  KeyCode.RIGHT,  InputType.RIGHT);
            addBinding("NavEnter",  KeyCode.ENTER,  InputType.ENTER);
            addBinding("NavQuit",   KeyCode.Q,      InputType.QUIT);
            addBinding("NavEsc",    KeyCode.ESCAPE, InputType.QUIT);
            addBinding("NavReload", KeyCode.R,      InputType.RELOAD);
            inputRegistered = true;
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void addBinding(String name, KeyCode key, InputType type) {
        try {
            FXGL.getInput().addAction(new UserAction(name) {
                @Override
                protected void onActionBegin() {
                    gameInput.push(type);
                }
            }, key);
        } catch (IllegalArgumentException ignored) {
            // already registered
        }
    }

    private void updateSelection(List<Text> buttons, int selected, Color selectColor,
                                 List<Paint> origColors, List<Double> baseXs, Text indicator) {
        for (int i = 0; i < buttons.size(); i++) {
            Text btn = buttons.get(i);
            boolean active = (i == selected);
            Color target = active ? selectColor : (Color) origColors.get(i);
            double targetX = active ? baseXs.get(i) + BTN_HOVER_SHIFT : baseXs.get(i);
            FxglUi.animateFill(btn, target, SELECT_ANIM);
            FxglUi.animateTranslateX(btn, targetX, SELECT_ANIM);
        }
        if (indicator != null) {
            Text activeBtn = buttons.get(selected);
            double toX = baseXs.get(selected) + BTN_HOVER_SHIFT - INDICATOR_OFFSET;
            double toY = activeBtn.getTranslateY();
            TranslateTransition tt = new TranslateTransition(SELECT_ANIM, indicator);
            tt.setToX(toX);
            tt.setToY(toY);
            tt.play();
        }
    }

    /** Convenience for {@link #waitForConfirm} on the same list order. */
    public static List<Text> arrayList(Text... items) {
        List<Text> l = new ArrayList<>();
        for (Text t : items) l.add(t);
        return l;
    }
}
