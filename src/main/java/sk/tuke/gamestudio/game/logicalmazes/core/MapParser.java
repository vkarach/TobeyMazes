package sk.tuke.gamestudio.game.logicalmazes.core;

import static sk.tuke.gamestudio.game.logicalmazes.core.FileReader.readFileLines;
import java.util.List;


public class MapParser {



    public MapParser(String filename) {
        List<String> mapLines = readFileLines(filename);
        Field mapField = parseMap(mapLines);
    }

    private Field parseMap(List<String> mapLines) {
        int[] WH = parseMapSize(mapLines);
        int width = WH[0];
        int height = WH[1];

        Tile[][] tiles = parseMapTiles(mapLines, width, height);
        boolean[][] vWalls = parseVertWalls(mapLines, width, height);
        boolean[][] hWalls = parseHorzWalls(mapLines, width, height);

        return new Field(tiles, vWalls, hWalls);
    }

    private int[] parseMapSize(List<String> lines) {
        int i = indexOfLineContains(lines, "W=");
        String line = lines.get(i);
        String[] parts = line.split(" ");
        int width = Integer.parseInt(parts[0].substring(2));
        int height = Integer.parseInt(parts[1].substring(2));

        return new int[]{width, height};
    }

    private Tile[][] parseMapTiles(List<String> lines, int width, int height) {
        int i = indexOfLineContains(lines, "TILES");
        int start = i + 1;
//        int end = start + height; // <-- todo: legal format check
        Tile[][] tiles = new Tile[height][width];
        for (int row = 0; row < height; row++) {
            String line = lines.get(start + row);

            if (line.length() != width) {
                throw new IllegalArgumentException(); // todo: msg
            }

            for (int col = 0; col < width; col++) {
                char ch = line.charAt(col);
                Tile tile = charToTile(ch);
                tiles[row][col] = tile;
            }
        }
        return tiles;
    }

    private boolean[][] parseVertWalls(List<String> lines, int width, int height) {
        int i = indexOfLineContains(lines, "VERT");
        int start = i + 1;
//        int end = start + height; // <-- todo: legal format check
        boolean[][] vWalls = new boolean[height][width + 1];
        for (int row = 0; row < height; row++) {
            String line = lines.get(start + row);

            if (line.length() != width + 1) {
                throw new IllegalArgumentException(); // todo: msg
            }

            for (int col = 0; col < width + 1; col++) {
                boolean isWall = line.charAt(col) == '1';
                vWalls[row][col] = isWall;
            }
        }
        return vWalls;
    }

    private boolean[][] parseHorzWalls(List<String> lines, int width, int height) {
        int i = indexOfLineContains(lines, "HORZ");
        int start = i + 1;
//        int end = start + height; // <-- todo: legal format check
        boolean[][] hWalls = new boolean[height + 1][width];
        for (int row = 0; row < height + 1; row++) {
            String line = lines.get(start + row);

            if (line.length() != width) {
                throw new IllegalArgumentException(); // todo: msg
            }

            for (int col = 0; col < width; col++) {
                boolean isWall = line.charAt(col) == '1';
                hWalls[row][col] = isWall;
            }
        }
        return hWalls;
    }

    private Tile charToTile(char ch) {
        return switch(ch) {
            case 'S' -> new Tile(TileType.PLAYER_SPAWN);
            case '!' -> new Tile(TileType.DESTINATION);
            case '.' -> new Tile(TileType.CLEAR);
            default  -> throw new IllegalArgumentException(); // todo: msg
        };
    }
    private int indexOfLineContains(List<String> lines, String part) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(part)) {
                return i;
            }
        }
        return -1;
    }
}
