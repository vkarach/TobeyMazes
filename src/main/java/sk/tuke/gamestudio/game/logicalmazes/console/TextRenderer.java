package sk.tuke.gamestudio.game.logicalmazes.console;

import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextRenderer {
    private final Console console;
    private final Map<String, String> replacers = new HashMap<>();

    public TextRenderer(Console console) {
        this.console = console;
        replacers.put("\\033", "\033");
        replacers.put("YELLOW", "\033[33m");
        replacers.put("OFF", "\033[0m");
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
        renderFromFile(filepath, 0, 0);
    }

    public void renderFromFile(String filepath, int x, int y) {
        List<String> lines = FileReader.readFileLines(filepath);
        synchronized (console.consoleLock) {
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                line = parseString(line);
                console.print(line, x, y + i);
            }
        }
    }
}
