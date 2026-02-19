package sk.tuke.gamestudio.game.logicalmazes.core;

public class Field { // record class mb
    private final Tile[][] tiles;
    private final int rowCount;
    private final int colCount;

    public Field(Tile[][] tiles, int rowCount, int colCount) {
        this.tiles = tiles;
        this.rowCount = rowCount;
        this.colCount = colCount;
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
}
