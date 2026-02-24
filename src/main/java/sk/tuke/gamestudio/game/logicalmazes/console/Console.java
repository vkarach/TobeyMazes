package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

import java.io.PrintWriter;

public class Console {
    public enum InputAction { UP, DOWN, LEFT, RIGHT, QUIT, NONE }

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

    public InputAction readAction() throws Exception {
        int ch = reader.read(10);

        if (ch == 'q') return InputAction.QUIT;
        if (ch != 27) return InputAction.NONE; // not ESC

        int second = reader.read(300);
        if (second < 0) return InputAction.NONE;

        if (second == '[' || second == 'O') {
            int third = reader.read(300);
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

    public void print(String text) {
        out.print(text);
    }

    public void print(char ch) {
        out.print(ch);
    }

    private void setCursorPosition(int x, int y) {
        terminal.puts(InfoCmp.Capability.cursor_address, y, x);
    }

    public void print(String text, int x, int y) {
        setCursorPosition(x, y);
        out.print(text);
    }

//    public void print(String text, int x, int y, int fgColor) {
//        setCursorPosition(x, y);
//
//        AttributedStyle style = AttributedStyle.DEFAULT.foreground(fgColor);
//        new AttributedString(text, style).print(terminal);
//    }

    public void close() throws Exception {
        terminal.puts(InfoCmp.Capability.cursor_normal);
        terminal.flush();
        terminal.close();
    }
}