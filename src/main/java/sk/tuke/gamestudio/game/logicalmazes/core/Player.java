package sk.tuke.gamestudio.game.logicalmazes.core;

public class Player {
    private int x;
    private int y;

    public Player(int x, int y) {
        setPosition(x, y);
    }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Player{x="+x + " y="+y +"}";
    }
}
