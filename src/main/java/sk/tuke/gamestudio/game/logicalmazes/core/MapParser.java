package sk.tuke.gamestudio.game.logicalmazes.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MapParser {
    private Field mapField;
    private Player player;

    public MapParser(String filename) {
        List<String> lines = readFileLines(filename);
        parseMap(lines);
//        this.player = findPlayer(mapField);
    }

    private InputStream getInputStream(String filename) {
        InputStream stream = MapParser.class
                .getClassLoader()
                .getResourceAsStream(filename);

        if (stream == null) {
            throw new RuntimeException("Resource not found: " + filename);
        }
        return stream;
    }

    private List<String> readFileLines(String filename) {
        InputStream stream = getInputStream(filename);

        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        return lines;
    }

    private Player parsePlayerLine(String line) {
        Player player;
        try {
            String playerXY = line.split("Player:")[1].strip();
            String[] parts = playerXY.split(" ");
            int player_x = Integer.parseInt(parts[0]);
            int player_y = Integer.parseInt(parts[1]);
            player = new Player(player_x, player_y);
        }
        catch (Exception e) {
            player = new Player(1,1); // player not found, setting default coordinates
        }

        return player;
    }

    private void parseMap(List<String> lines) {
        this.player = parsePlayerLine(lines.removeFirst());

        int rowCount = lines.size(); // ????
        int colCount = lines.getFirst().length(); // ???

        Tile[][] tiles = new Tile[rowCount][colCount];
        for (int row = 0; row < rowCount; row++) {
            String line = lines.get(row);

            for (int col = 0; col < colCount; col++) {
                char c = line.charAt(col);
                Tile tile = parseCharacter(c);
                tiles[row][col] = tile;
            }
        }
        this.mapField = new Field(tiles);
    }

    private Tile parseCharacter(char c) {
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

    public Field getMapField() {
        return mapField;
    }

    public Player getPlayer() {
        return player;
    }
}
