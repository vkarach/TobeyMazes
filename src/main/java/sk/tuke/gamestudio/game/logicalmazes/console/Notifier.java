package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;

@Component
public class Notifier {
    private final Console console;

    private final SoundUtil errorSound = new SoundUtil("sounds/error.wav");

    public Notifier(Console console) {
        this.console = console;
    }

//    public void showError(String msg, int x, int y) {
//        Thread error = new Thread(() -> {
//            showErrorImpl(msg, x, y);
//        });
//        error.setDaemon(true);
//        error.start();
//    }

    public void showError(String msg, int x, int y) {
        int clearLen = msg.length();

        errorSound.play();
        console.print(
                msg,
                x, y,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)
        );

        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        console.drainInput();

        console.clearLine(clearLen, x, y);
    }
}
//    public void warnIfTerminalTooSmall(int expectedHeight) {
//        int x = terminal.getWidth() - 40;
//        int y = 0;
//        AttributedStyle style = AttributedStyle.DEFAULT.inverse();
//        while (true) {
//            int h = terminal.getHeight();
//
//            synchronized (consoleLock) {
//                if (h < expectedHeight) {
//                    moveCursorToStart();
//                    print("Increase your terminal size (Ctrl -)", x, y, style);
//                    print(String.format("your height: %d expected: >%d", h, expectedHeight), x, y + 1, style);
//                } else {
//                    print(" ".repeat(50), x, y);
//                    print(" ".repeat(50), x, y + 1);
//                }
//            }
//
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException e) {
//                return;
//            }
//        }
//    }
