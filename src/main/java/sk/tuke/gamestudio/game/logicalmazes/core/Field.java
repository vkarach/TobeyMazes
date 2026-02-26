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

    public boolean canStep(Player player, Direction direction) {
        int x = player.getX();
        int y = player.getY();

        return switch (direction) {
            case DOWN -> (y + 1 < rowCount) && !hWalls[y + 1][x];
            case UP -> (y > 0) && !hWalls[y][x];
            case LEFT -> (x > 0) && !vWalls[y][x];
            case RIGHT -> (x + 1 < colCount) && !vWalls[y][x + 1];
            case NONE -> false;
        };
    }

    public void step(Player player, Direction direction) {
        if (!canStep(player, direction)) return;
        switch (direction) {
            case DOWN -> player.setY(player.getY() + 1);
            case UP -> player.setY(player.getY() - 1);
            case LEFT -> player.setX(player.getX() - 1);
            case RIGHT -> player.setX(player.getX() + 1);
        }
    }

    public boolean onTarget(Player player) {
        return tiles[player.getY()][player.getX()].getType() == TileType.TARGET;
    }

    public boolean takeTarget(Player player) {
        if (onTarget(player)) {
            tiles[player.getY()][player.getX()].setType(TileType.CLEAR);
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
