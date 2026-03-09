package sk.tuke.gamestudio.game.logicalmazes.core;

import java.util.HashSet;
import java.util.Set;

public enum Level {
    INTRODUCTION(1,"Level 1", Difficulty.EASY, "maps/level_1.txt"),
    IDK_FOR_NOW(2,"Level 2", Difficulty.EASY, "maps/level_2.txt");

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    private final int id;
    private final String title;
    private final Difficulty difficulty;
    private final String filepath;
//    private long record;

    Level(int id, String title, Difficulty difficulty, String filepath) {
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
            String filepath = level.getFilepath();
            if (!FileReader.checkFileExists(filepath)) {
                throw new IllegalArgumentException(String.format("file %s not exist!", filepath));
            }
            try {
                new MapParser().parseMap(filepath); // check for the correct map format
            }
            catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format("level (id=%d) with map (file=%s) is not valid", level.getId(), filepath), e
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
