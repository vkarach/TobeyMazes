package sk.tuke.gamestudio.game.logicalmazes.core;

public class Field {
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

    public boolean canStep(Player p, Direction dir) {
        int x = p.getX();
        int y = p.getY();

        return switch (dir) {
            case DOWN -> (y + 1 < rowCount) && !hWalls[y + 1][x];
            case UP -> (y > 0) && !hWalls[y][x];
            case LEFT -> (x > 0) && !vWalls[y][x];
            case RIGHT -> (x + 1 < colCount) && !vWalls[y][x + 1];
            case NONE -> false;
        };
    }

    public void step(Player p, Direction dir) {
        switch (dir) {
            case DOWN -> p.setY(p.getY() + 1);
            case UP -> p.setY(p.getY() - 1);
            case LEFT -> p.setX(p.getX() - 1);
            case RIGHT -> p.setX(p.getX() + 1);
        }
    }

    public boolean onTarget(Player p) {
        return tiles[p.getY()][p.getX()].getType() == TileType.TARGET;
    }

    public boolean takeTarget(Player p) {
        if (onTarget(p)) {
            tiles[p.getY()][p.getX()].setType(TileType.CLEAR);
            return true;
        }
        return false;
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
