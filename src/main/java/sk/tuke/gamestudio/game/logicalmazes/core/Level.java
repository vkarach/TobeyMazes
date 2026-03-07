package sk.tuke.gamestudio.game.logicalmazes.core;

import java.util.HashSet;
import java.util.Set;

public enum Level {
    INTRODUCTION(1,"Introduction", Difficulty.EASY, "maps/map_1.txt"),
    IDK_FOR_NOW(2,"Unnamed :)", Difficulty.EASY, "maps/map_2.txt");

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    private final int id;
    private final String title;
    private final Difficulty difficulty;
    private final String filepath;
//    private long record;

    Level(int id, String title, Difficulty difficulty, String filepath) {
        if (!FileReader.checkFileExists(filepath)) {
            throw new IllegalArgumentException(String.format("file %s not exist!", filepath));
        }
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.filepath = filepath;

    }

    static { // check for unique id
        Set<Integer> ids = new HashSet<>();
        for (Level level : Level.values()) {
            if (!ids.add(level.id)) {
                throw new IllegalStateException(
                        String.format("level with id %d already exists!", level.id)
                );
            }
        }
    }

    public int getId() {
        return id;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-5s", title, difficulty);
    }
}
