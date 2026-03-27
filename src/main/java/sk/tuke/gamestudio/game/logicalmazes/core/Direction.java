package sk.tuke.gamestudio.game.logicalmazes.core;

public enum Direction {
    LEFT, RIGHT, UP, DOWN, NONE;

    public static Direction InputToDirection(InputType inputType) {
        for (Direction d : Direction.values()) {
            if (d.name().equals(inputType.name())) {
                return d;
            }
        }
        return NONE;
    }

}
