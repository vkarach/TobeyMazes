package sk.tuke.gamestudio.game.logicalmazes.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileReader {
    private static InputStream getInputStream(String filepath) {
        InputStream stream = MapParser.class
                .getClassLoader()
                .getResourceAsStream(filepath);

        if (stream == null) {
            throw new RuntimeException("Resource not found: " + filepath);
        }
        return stream;
    }

    public static boolean checkFileExists(String filepath) {
        InputStream stream = MapParser.class
                .getClassLoader()
                .getResourceAsStream(filepath);

        return stream != null;
    }

    public static List<String> readFileLines(String filepath) {
        InputStream stream = getInputStream(filepath);

        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        return lines;
    }
}
