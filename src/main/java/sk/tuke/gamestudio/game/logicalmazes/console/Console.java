package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Attributes;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.*;

import java.io.PrintWriter;

public class Console {
    private final Terminal terminal;
    private final NonBlockingReader reader;
    private final LineReader lineReader;
    private final PrintWriter out;

    private Attributes originalAttributes;

    public final Object consoleLock = new Object();

    public Console() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .jna(true)
                    .build();
        }
        catch (Exception e) {
            throw new RuntimeException("can not launch console");
        }
        enterRawMode();
        reader = terminal.reader();
        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        this.out = terminal.writer();
        terminal.flush();
    }

    public void enterRawMode() {
        originalAttributes = terminal.enterRawMode();
        terminal.flush();
        terminal.puts(InfoCmp.Capability.cursor_invisible);
    }

    public void exitRawMode() {
        terminal.puts(InfoCmp.Capability.cursor_normal);
        terminal.flush();
        if (originalAttributes == null) {
            return;
        }
        terminal.setAttributes(originalAttributes);
    }

    private int readInput(long timeout) {
        try {
            return reader.read(timeout);
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot read input", e);
        }
    }

    public InputType readAction() { // todo: upper case
        int ch = readInput(50);

        if (ch == 'q' || ch == -1 || ch == 4) return InputType.QUIT;
        else if (ch == 'r') return InputType.RELOAD;
        else if (ch == '\r' || ch == '\n') return InputType.ENTER;
        if (ch != 27) return InputType.NONE; // not ESC

        int second = readInput(100);
        if (second < 0) return InputType.NONE;

        if (second == '[' || second == 'O') {
            int third = readInput(100);
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

    public String readLine(String prompt) {
        try {
            return lineReader.readLine(prompt);
        }
        catch (UserInterruptException | EndOfFileException e) {
            return null;
        }
    }

    public void clear() {
        synchronized (consoleLock) {
            terminal.puts(InfoCmp.Capability.clear_screen);
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

    public void setCursorPosition(int x, int y) {
        synchronized (consoleLock) {
            terminal.puts(InfoCmp.Capability.cursor_address, y, x);
        }
    }

    public void print(String text) {
        synchronized (consoleLock) {
            out.print(text);
        }
    }

    public void print(char ch) {
        synchronized (consoleLock) {
            out.print(ch);
        }
    }

    public void print(String text, int x, int y) {
        synchronized (consoleLock) {
            setCursorPosition(x, y);
            print(text);
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
            setCursorPosition(x, y);
            print(text, style);
        }
    }

    public void print(AttributedStringBuilder asb, int x, int y) {
        synchronized (consoleLock) {
            setCursorPosition(x, y);

            asb.toAttributedString().print(terminal);
            terminal.flush();
        }
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