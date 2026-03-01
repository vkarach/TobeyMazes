package sk.tuke.gamestudio.game.logicalmazes.console;

import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;

import java.util.List;

public class TextRenderer {
    private final Console console;

    public TextRenderer(Console console) {
        this.console = console;
    }

    public void renderFromFile(String filepath, int x, int y) {
        List<String> lines = FileReader.readFileLines(filepath);
        for (int i = 0; i < lines.size(); i++) {
            console.print(lines.get(i), x, y + i);
        }
    }
}
