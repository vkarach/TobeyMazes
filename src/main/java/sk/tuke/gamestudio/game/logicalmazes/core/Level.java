package sk.tuke.gamestudio.game.logicalmazes.core;

import java.util.HashSet;
import java.util.Set;

public enum Level {
    LEVEL_1(1,"Level 1", Difficulty.EASY,   "levels/level_1.txt"),
    LEVEL_2(2,"Level 2", Difficulty.NORMAL, "levels/level_2.txt"),
    LEVEL_3(3,"Level 3", Difficulty.NORMAL, "levels/level_3.txt"),
    LEVEL_4(4,"Level 4", Difficulty.MEDIUM, "levels/level_4.txt"), // NORMAL ?
    LEVEL_5(5,"Level 5", Difficulty.MEDIUM,   "levels/level_5.txt"),
    LEVEL_6(6,"Level 6", Difficulty.HARD,   "levels/level_6.txt"),
    LEVEL_7(7,"Level 7", Difficulty.NORMAL,   "levels/level_7.txt"),
    LEVEL_8(8,"Level 8", Difficulty.MEDIUM,   "levels/level_8.txt"),
    LEVEL_9(9,"Level 9", Difficulty.NORMAL,   "levels/level_9.txt"),
    LEVEL_10(10,"Level 10", Difficulty.MEDIUM,   "levels/level_10.txt"),
    LEVEL_11(11,"Level 11", Difficulty.HARD,   "levels/level_11.txt");

    public enum Difficulty {
        EASY, NORMAL, MEDIUM, HARD
    }

    private final int id;
    private final String title;
    private final Difficulty difficulty;
    private final String filepath;

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

//    @Override
//    public String toString() {
//        return String.format("%-15s %-8s", title, difficulty);
//    }
}
