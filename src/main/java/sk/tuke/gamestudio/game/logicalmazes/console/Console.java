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

    public InputType readAction() {
        int ch = readInput(10);

        if (ch == 'q') return InputType.QUIT;
        else if (ch == '\r' || ch == '\n') return InputType.ENTER;
        if (ch != 27) return InputType.NONE; // not ESC

        int second = readInput(300);
        if (second < 0) return InputType.NONE;

        if (second == '[' || second == 'O') {
            int third = readInput(300);
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

    public String readLine(String prompt) {
        try {
            return lineReader.readLine(prompt);
        }
        catch (UserInterruptException | EndOfFileException e) {
            return null;
        }
    }

    public void clear() {
        terminal.puts(InfoCmp.Capability.clear_screen);
    }

    public void moveCursorToStart() {
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
    }

    public void setCursorPosition(int x, int y) {
        terminal.puts(InfoCmp.Capability.cursor_address, y, x);
    }

    public void print(String text) {
        out.print(text);
    }

    public void print(char ch) {
        out.print(ch);
    }

    public void print(String text, int x, int y) {
        setCursorPosition(x, y);
        print(text);
    }

    public void print(String text, AttributedStyle style) {
        new AttributedString(text, style).print(terminal);
        terminal.flush();
    }

    public void print(String text, int x, int y, AttributedStyle style) {
        setCursorPosition(x, y);
        print(text, style);
    }

    public void print(AttributedStringBuilder asb, int x, int y) {
        setCursorPosition(x, y);

        asb.toAttributedString().print(terminal);
        terminal.flush();
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