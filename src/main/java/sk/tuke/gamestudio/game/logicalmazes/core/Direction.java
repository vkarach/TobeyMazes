package sk.tuke.gamestudio.game.logicalmazes.core;

import sk.tuke.gamestudio.game.logicalmazes.console.Console;

public enum Direction {
    LEFT, RIGHT, UP, DOWN, NONE;

    public static Direction InputToDirection(Console.InputAction input) {
        for (Direction d : Direction.values()) {
            if (d.name().equals(input.name())) {
                return d;
            }
        }
        return NONE;
    }

}
