package sk.tuke.gamestudio.game.logicalmazes.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MapParser {
    private static InputStream getInputStream(String filename) {
        InputStream stream = MapParser.class
                .getClassLoader()
                .getResourceAsStream(filename);

        if (stream == null) {
            throw new RuntimeException("Resource not found: " + filename);
        }
        return stream;
    }

    public static Field parseMap(String mapFilename) {
        InputStream input = getInputStream(mapFilename);

        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(input)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        int rowCount = lines.size();
        int colCount = lines.get(0).length();

        Tile[][] tiles = new Tile[rowCount][colCount];

        for (int row = 0; row < rowCount; row++) {
            String line = lines.get(row);

            for (int col = 0; col < colCount; col++) {
                char c = line.charAt(col);
                Tile tile = parseCharacter(c);

                tiles[row][col] = tile;
            }
        }

        return new Field(tiles);
    }
    private static Tile parseCharacter(char c) {
        TileType type = switch (c) {
            case '.' -> TileType.CLEAR;
            case '-' -> TileType.HORIZONTAL_WALL;
            case '|' -> TileType.VERTICAL_WALL;
            case 'A' -> TileType.PLAYER_SPAWN;
            case '!' -> TileType.DESTINATION;
            default -> throw new RuntimeException("Invalid character in mapFile" + c);
        };
        return new Tile(type);
    }
}
