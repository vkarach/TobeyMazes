package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

public class Console {

    public enum Action { UP, DOWN, LEFT, RIGHT, QUIT, NONE }

    private final Terminal terminal;
    private final NonBlockingReader reader;

    public Console() throws Exception {
        terminal = TerminalBuilder.builder()
                .system(true)
                .jna(true)
                .build();
        terminal.enterRawMode();
        reader = terminal.reader();
    }

    public Action readAction() throws Exception {
        int ch = reader.read();

        if (ch == 'q') return Action.QUIT;
        if (ch != 27) return Action.NONE; // not ESC

        int second = reader.read(300);
        if (second < 0) return Action.NONE;

        if (second == '[' || second == 'O') {
            int third = reader.read(300);
            if (third < 0) return Action.NONE;

            return switch (third) {
                case 'A' -> Action.UP;
                case 'B' -> Action.DOWN;
                case 'C' -> Action.RIGHT;
                case 'D' -> Action.LEFT;
                default -> Action.NONE;
            };
        }

        return Action.NONE;
    }
    public void clear() {
        terminal.puts(InfoCmp.Capability.clear_screen);
    }

    public void close() throws Exception {
        terminal.close();
    }
}