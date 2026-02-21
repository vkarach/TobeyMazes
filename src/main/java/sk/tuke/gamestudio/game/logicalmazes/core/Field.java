package sk.tuke.gamestudio.game.logicalmazes.core;

public class Field { // record class mb
    private final Tile[][] tiles;
    private final boolean[][] vWalls;
    private final boolean[][] hWalls;
    private final int rowCount;
    private final int colCount;

    public Field(Tile[][] tiles, boolean[][] vWalls, boolean[][] hWalls) {
        this.tiles = tiles;
        this.vWalls = vWalls;
        this.hWalls = hWalls;
        this.rowCount = tiles.length;
        this.colCount = tiles.length > 0 ? tiles[0].length : 0;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public boolean[][] getVWalls() {
        return vWalls;
    }

    public boolean[][] getHWalls() {
        return hWalls;
    }
}
