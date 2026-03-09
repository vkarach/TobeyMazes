package sk.tuke.gamestudio.game.logicalmazes.core;

import static sk.tuke.gamestudio.game.logicalmazes.core.FileReader.readFileLines;
import java.util.List;

public class MapParser {
    public static class Result {
        public Field mapField;
        public Player player;
        public int targetCount;

        public Result(Field field, Player player, int targetCount) {
            this.mapField = field;
            this.player = player;
            this.targetCount = targetCount;
        }
    }
    private Player player;      // maybe add function parsePlayer
    private int targetCount;    // maybe add function parseTargetCount

    public Result parseMap(String filepath) {
        List<String> mapLines = readFileLines(filepath);
        int[] wh = parseMapSize(mapLines);
        int width = wh[0];
        int height = wh[1];

        Tile[][] tiles = parseMapTiles(mapLines, width, height); // also parses player and targetCount

        boolean[][] vWalls = parseVertWalls(mapLines, width, height);

        boolean[][] hWalls = parseHorzWalls(mapLines, width, height);

        Field field = new Field(tiles, vWalls, hWalls);

        Result result = new Result(field, player, targetCount);
        player = null;
        targetCount = 0;

        return result;
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
//        int end = startLaunchLevel + height; // <-- todo: legal format check
        Tile[][] tiles = new Tile[height][width];
        for (int row = 0; row < height; row++) {
            String line = lines.get(start + row);

            if (line.length() != width) {
                throw new IllegalArgumentException(
                        "TILES section: invalid row length at row=" + row +
                        ", expected=" + width +
                        ", got=" + line.length() +
                        ", line=\"" + line + "\""
                );
            }

            for (int col = 0; col < width; col++) {
                char ch = line.charAt(col);
                Tile tile = charToTile(ch);

                if (tile.getType() == TileType.PLAYER_SPAWN) {
                    if (this.player != null) {
                        throw new IllegalArgumentException("multiple player spawns");
                    }
                    this.player = new Player(col, row);
                }
                else if (tile.getType() == TileType.TARGET) {
                    this.targetCount++;
                }

                tiles[row][col] = tile;
            }
        }
        if (this.player == null) {
            throw new IllegalArgumentException("no player spawn");
        }
        if (this.targetCount == 0) {
            throw new IllegalArgumentException("no target found");
        }
        return tiles;
    }

    private boolean[][] parseVertWalls(List<String> lines, int width, int height) {
        int i = indexOfLineContains(lines, "VERT");
        int start = i + 1;
//        int end = startLaunchLevel + height; // <-- todo: legal format check
        boolean[][] vWalls = new boolean[height][width + 1];
        for (int row = 0; row < height; row++) {
            String line = lines.get(start + row);

            if (line.length() != width + 1) {
                throw new IllegalArgumentException(String.format(
                        "VERT section: invalid row length at row=%d (expected= %d, got=%d)",
                        row, width + 1, line.length())
                );
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
//        int end = startLaunchLevel + height; // <-- todo: legal format check
        boolean[][] hWalls = new boolean[height + 1][width];
        for (int row = 0; row < height + 1; row++) {
            String line = lines.get(start + row);

            if (line.length() != width) {
                throw new IllegalArgumentException(
                    "HORZ section: invalid row length at row=" + row +
                    " (expected=" + width + ", got=" + line.length() + ")"
                );
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
            case '!' -> new Tile(TileType.TARGET);
            case '.' -> new Tile(TileType.CLEAR);
            default  -> throw new IllegalArgumentException("unknown tile symbol: " + ch);
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
