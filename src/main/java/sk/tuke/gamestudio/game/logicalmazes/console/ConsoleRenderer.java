package sk.tuke.gamestudio.game.logicalmazes.console;

import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleRenderer {
    private final Console console;
    private final Map<String, String> replacers = new HashMap<>();

    public ConsoleRenderer(Console console) {
        this.console = console;
        replacers.put("\\033", "\033");
        replacers.put("YELLOW", "\033[33m");
        replacers.put("OFF", "\033[0m");
        replacers.put("END", "");
    }

    public record RenderSize(int width, int height) {}

    public RenderSize getRenderFromFileSize(String filepath) {
        List<String> lines = FileReader.readFileLines(filepath);
        int width = 0;
        int height = 0;
        for (String line : lines) {
            width = Math.max(width, line.length());
            height++;
        }
        return new RenderSize(width, height);
    }

    public String parseString(String str) {
        for (Map.Entry<String, String> replacer : replacers.entrySet()) {
            str = str.replace(replacer.getKey(), replacer.getValue());
        }
        return str;
    }

    public void renderStringList(String[] strList, int x, int y) {
        for (int i = 0; i < strList.length; i++) {
            console.print(strList[i], x, y + i);
        }
    }

    public void renderFromFile(String filepath) {
        renderFromFile(filepath, 0, 0, false);
    }

    public void renderFromFile(String filepath, int x, int y) {
        renderFromFile(filepath, x, y, false);
    }

    public void renderFromFile(String filepath, int x, int y, boolean reverse) {
        List<String> lines = FileReader.readFileLines(filepath);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = parseString(line);
            if (reverse) {
                line = mirrorLine(line);
            }
            console.print(line, x, y + i);
        }
    }

    public Thread renderAnimation(String filepath, int frameTimeMs, int x, int y) {
        List<String> lines = FileReader.readFileLines(filepath);

        List<List<String>> frames = splitFrames(lines);

        Thread animationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                renderAnimation(frames, frameTimeMs, x, y);
            }
        });
        animationThread.setDaemon(true);
        animationThread.start();
        return animationThread;
    }

    private String mirrorLine(String line) {
        StringBuilder reversed = new StringBuilder(line).reverse();
        for (int i = 0; i < reversed.length(); i++) {
            char c = reversed.charAt(i);

            switch (c) {
                case '/': reversed.setCharAt(i, '\\'); break;
                case '\\': reversed.setCharAt(i, '/'); break;
                case '(': reversed.setCharAt(i, ')'); break;
                case ')': reversed.setCharAt(i, '('); break;
                case '<': reversed.setCharAt(i, '>'); break;
                case '>': reversed.setCharAt(i, '<'); break;
                case '[': reversed.setCharAt(i, ']'); break;
                case ']': reversed.setCharAt(i, '['); break;
                case '{': reversed.setCharAt(i, '}'); break;
                case '}': reversed.setCharAt(i, '{'); break;
            }
        }
        return reversed.toString();
    }

    private void renderAnimation(List<List<String>> frames, int frameTimeMs, int x, int y) {
        for (List<String> frame : frames) {
            int ty = y;

            for (String line : frame) {
                console.print(line, x, ty++);
            }

            try {
                Thread.sleep(frameTimeMs);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private List<List<String>> splitFrames(List<String> lines) {
        List<List<String>> frames = new ArrayList<>();
        List<String> current = new ArrayList<>();

        for (String line : lines) {
            line = parseString(line);
            if (line != null && line.trim().equals("FRAME")) {
                if (!current.isEmpty()) {
                    frames.add(current);
                    current = new ArrayList<>();
                }
            } else {
                current.add(line == null ? "" : line);
            }
        }

        if (!current.isEmpty()) {
            frames.add(current);
        }

        return frames;
    }
}
