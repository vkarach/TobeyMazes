package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.terminal.TerminalBuilder;
import org.jline.utils.*;
import org.jline.terminal.Terminal;

import java.io.PrintWriter;

public class Console {
    public enum InputAction { UP, DOWN, LEFT, RIGHT, ENTER, QUIT, NONE }

    private final Terminal terminal;
    private final NonBlockingReader reader;
    private final PrintWriter out;

    public Console() throws Exception {
        terminal = TerminalBuilder.builder()
                .system(true)
                .jna(true)
                .build();
        terminal.enterRawMode();
        reader = terminal.reader();
        this.out = terminal.writer();
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();
    }

    private int readInput(long timeout) {
        try {
            return reader.read(timeout);
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot read input", e);
        }
    }

    public InputAction readAction() {
        int ch = readInput(10);

        if (ch == 'q') return InputAction.QUIT;
        else if (ch == '\r' || ch == '\n') return InputAction.ENTER;
        if (ch != 27) return InputAction.NONE; // not ESC

        int second = readInput(300);
        if (second < 0) return InputAction.NONE;

        if (second == '[' || second == 'O') {
            int third = readInput(300);
            if (third < 0) return InputAction.NONE;

            return switch (third) {
                case 'A' -> InputAction.UP;
                case 'B' -> InputAction.DOWN;
                case 'C' -> InputAction.RIGHT;
                case 'D' -> InputAction.LEFT;
                default -> InputAction.NONE;
            };
        }

        return InputAction.NONE;
    }
    public void clear() {
        terminal.puts(InfoCmp.Capability.clear_screen);
    }

    public void moveCursorToStart() {
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
    }

    private void setCursorPosition(int x, int y) {
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
        terminal.puts(InfoCmp.Capability.cursor_normal);
        terminal.flush();
        try {
            terminal.close();
        }
        catch (Exception e) {
            throw new RuntimeException("can not close console", e);
        }
    }
}