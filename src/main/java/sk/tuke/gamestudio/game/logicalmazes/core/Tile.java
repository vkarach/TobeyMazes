package sk.tuke.gamestudio.game.logicalmazes.core;

public class Tile {
    private TileType state;

    public Tile(TileType state) {
        this.state = state;
    }

    public TileType getState() {
        return state;
    }

    public void setState(TileType state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.valueOf(state);
    }
}
