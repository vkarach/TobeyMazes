package sk.tuke.gamestudio.game.logicalmazes.console;

import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;

import java.util.List;

public class KonekTobeyAnimation {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;

    private static final String FILE_PATH = "uiTexts/konek_tobey.txt";
    private final ConsoleRenderer.RenderSize size;
    private final List<String> sprite;

    public KonekTobeyAnimation(Console console, ConsoleRenderer consoleRenderer) {
        this.consoleRenderer = consoleRenderer;
        this.console = console;
        this.size = consoleRenderer.getRenderFromFileSize(FILE_PATH);
        this.sprite = FileReader.readFileLines(FILE_PATH);
    }

    private void clearSprite(int x, int y) {
        String blankLine = " ".repeat(size.width());
        for (int row = 0; row < size.height(); row++) {
            console.print(blankLine, x, y + row);
        }
    }

    private void render(int x, int y, boolean mirror) {
        for (int i = 0; i < sprite.size(); i++) {
            String line = sprite.get(i);

            line = consoleRenderer.parseString(line);
            if (mirror) {
                line = consoleRenderer.mirrorLine(line);
            }

            console.print(line, x, y + i);
        }
    }

    public Thread startKonekTobeyAnimation(int x, int y) {
        int move = 30;
        int frameTime = 70;
        int turnPause = 220;

        console.print("-".repeat(move * 2), x - move/2, y + size.height());

        Thread thread = new Thread(() -> {
            int previousX = x;

            while (!Thread.currentThread().isInterrupted()) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                for (int i = 0; i <= move; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        clearSprite(previousX, y);
                        return;
                    }
                    long frameStart = System.currentTimeMillis();

                    int currentX = x + i;
                    clearSprite(previousX, y);
                    render(currentX, y, false);

                    previousX = currentX;
                    sleepExact(frameStart, frameTime);
                }

                sleep(turnPause);

                for (int i = move; i >= 0; i--) {
                    if (Thread.currentThread().isInterrupted()) {
                        clearSprite(previousX, y);
                        return;
                    }
                    long frameStart = System.currentTimeMillis();

                    int currentX = x + i;
                    clearSprite(previousX, y);
                    render(currentX, y, true);

                    previousX = currentX;
                    sleepExact(frameStart, frameTime);
                }

                sleep(turnPause);
            }

            clearSprite(previousX, y);
        });

        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private void sleepExact(long frameStart, int frameTime) {
        long elapsed = System.currentTimeMillis() - frameStart;
        long remaining = frameTime - elapsed;
        if (remaining > 0) {
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
