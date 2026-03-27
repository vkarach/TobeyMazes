package sk.tuke.gamestudio.game.logicalmazes.ui.console;

import org.jline.reader.*;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.*;
import sk.tuke.gamestudio.game.logicalmazes.ui.GameInput;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;

import java.io.PrintWriter;
import java.util.Arrays;

@Component
public class Console implements GameInput {
    private final Terminal terminal;
    private final NonBlockingReader reader;
    private final LineReader lineReader;
    private final PrintWriter out;

    public final Object consoleLock = new Object();

    private final SoundUtil clickSound =
            new SoundUtil("sounds/click.wav", 0.2f);

    private final SoundUtil submitSound =
            new SoundUtil("sounds/select.wav", 0.2f);

    private Attributes originalAttributes;

    public Console() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .jna(true)
                    .build();
        }
        catch (Exception e) {
            throw new RuntimeException("can not mainMenu console");
        }

        reader = terminal.reader();

        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        this.out = terminal.writer();

        wrapWidgetWithSound(LineReader.SELF_INSERT,          clickSound);
        wrapWidgetWithSound(LineReader.BACKWARD_DELETE_CHAR, clickSound);
        wrapWidgetWithSound(LineReader.ACCEPT_LINE,          submitSound);

        enterRawMode();
    }

    private void wrapWidgetWithSound(String widgetName, SoundUtil sound) {
        Widget original = lineReader.getBuiltinWidgets().get(widgetName);
        lineReader.getWidgets().put(widgetName, () -> {
            sound.play();
            return original.apply();
        });
    }

    public void enterRawMode() {
        originalAttributes = terminal.enterRawMode();
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();
    }

    public void exitRawMode() {
        terminal.puts(InfoCmp.Capability.cursor_normal);
        terminal.flush();
        if (originalAttributes == null) {
            return;
        }
        terminal.setAttributes(originalAttributes);
    }

    public String readLine(String prompt) {
        try {
            return lineReader.readLine(prompt);
        }
        catch (UserInterruptException | EndOfFileException e) {
            return null;
        }
    }

    private int getInput(long timeout) {
        try {
            return reader.read(timeout);
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot read input", e);
        }
    }

    public InputType getInput() {
        int ch = getInput(50);

        int[] quitKeys = new int[] {'q', 'Q', 'Й', 'й', 4, -1};
        int[] reloadKeys = new int[] {'r', 'R', 'к', 'К'};

        if (Arrays.stream(quitKeys).anyMatch(key -> key == ch)) {
            return InputType.QUIT;
        }
        if (Arrays.stream(reloadKeys).anyMatch(key -> key == ch)) {
            return InputType.RELOAD;
        }
        if (ch == '\r' || ch == '\n') {
            return InputType.ENTER;
        }
        if (ch != 27) { // not ESC
            return InputType.NONE;
        }

        int second = getInput(100);
        if (second < 0) return InputType.NONE;

        if (second == '[' || second == 'O') {
            int third = getInput(100);
            if (third < 0) return InputType.NONE;

            return switch (third) {
                case 'A' -> InputType.UP;
                case 'B' -> InputType.DOWN;
                case 'C' -> InputType.RIGHT;
                case 'D' -> InputType.LEFT;
                default -> InputType.NONE;
            };
        }

        return InputType.NONE;
    }

    public int getWidth() {
        return terminal.getWidth();
    }

    public int getHeight() {
        return terminal.getHeight();
    }

    public void clear() {
        synchronized (consoleLock) {
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
        }
    }

    public void clearLine(int x, int y) {
        clearLine(50, x, y);
    }

    public void clearLine(int clearLen, int x, int y) {
        print(" ".repeat(clearLen), x, y);
    }

    public void moveCursorToStart() {
        synchronized (consoleLock) {
            terminal.puts(InfoCmp.Capability.cursor_home);
            terminal.flush();
        }
    }

    private void moveCursor(int x, int y) {
        terminal.puts(InfoCmp.Capability.cursor_address, y, x);
    }

    public void setCursorPosition(int x, int y) {
        synchronized (consoleLock) {
            moveCursor(x, y);
            terminal.flush();
        }
    }

    public void print(String text) {
        synchronized (consoleLock) {
            out.print(text);
            terminal.flush();
        }
    }

    public void print(char ch) {
        synchronized (consoleLock) {
            out.print(ch);
            terminal.flush();
        }
    }

    public void print(String text, int x, int y) {
        synchronized (consoleLock) {
            moveCursor(x, y);
            out.print(text);
            terminal.flush();
        }
    }

    public void print(String text, AttributedStyle style) {
        synchronized (consoleLock) {
            new AttributedString(text, style).print(terminal);
            terminal.flush();
        }
    }

    public void print(String text, int x, int y, AttributedStyle style) {
        synchronized (consoleLock) {
            moveCursor(x, y);
            new AttributedString(text, style).print(terminal);
            terminal.flush();
        }
    }

    public void print(AttributedStringBuilder asb, int x, int y) {
        synchronized (consoleLock) {
            moveCursor(x, y);
            asb.toAttributedString().print(terminal);
            terminal.flush();
        }
    }

    public void drainInput() {
        try {
            while (reader.read(1) != NonBlockingReader.READ_EXPIRED) {continue;}
        }
        catch (Exception ignored) {}
    }

    public void close() {
        exitRawMode();
        try {
            terminal.close();
        }
        catch (Exception e) {
            throw new RuntimeException("can not close console", e);
        }
    }
}