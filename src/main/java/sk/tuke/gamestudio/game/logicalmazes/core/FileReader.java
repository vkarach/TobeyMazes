package sk.tuke.gamestudio.game.logicalmazes.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileReader {
    private static InputStream getInputStream(String filename) {
        InputStream stream = MapParser.class
                .getClassLoader()
                .getResourceAsStream(filename);

        if (stream == null) {
            throw new RuntimeException("Resource not found: " + filename);
        }
        return stream;
    }

    static List<String> readFileLines(String filename) {
        InputStream stream = getInputStream(filename);

        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        return lines;
    }
}
