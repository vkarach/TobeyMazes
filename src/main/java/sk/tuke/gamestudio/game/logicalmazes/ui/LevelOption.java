package sk.tuke.gamestudio.game.logicalmazes.ui;

import sk.tuke.gamestudio.game.logicalmazes.core.Level;

public enum LevelOption {
    LEVEL_1(Level.LEVEL_1, null),
    LEVEL_2(Level.LEVEL_2, null),
    LEVEL_3(Level.LEVEL_3, null),
    LEVEL_4(Level.LEVEL_4, null),
    LEVEL_5(Level.LEVEL_5, null),
    LEVEL_6(Level.LEVEL_6, null),
    LEVEL_7(Level.LEVEL_7, null),
    LEVEL_8(Level.LEVEL_8, null),
    LEVEL_9(Level.LEVEL_9, null),
    LEVEL_10(Level.LEVEL_10, null),
    LEVEL_11(Level.LEVEL_11, null),
    BACK(null, "Back");

    private final Level level;
    private String title;

    LevelOption(Level level, String title) {
        this.level = level;
        this.title = title;
    }

    public Level getLevel() {
        return level;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}