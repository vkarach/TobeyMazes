package sk.tuke.gamestudio.game.logicalmazes.core;

public enum Level {
    INTRODUCTION("Introduction", Difficulty.EASY, "maps/map_1.txt"),
    IDK_FOR_NOW("Unnamed", Difficulty.EASY, "maps/map_2.txt");

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    private final String title;
    private final Difficulty difficulty;
    private final String filepath;
//    private long record;

    Level(String title, Difficulty difficulty, String filepath) {
        if (!FileReader.checkFileExists(filepath)) {
            throw new IllegalArgumentException(String.format("file %s not exist!", filepath));
        }
        this.title = title;
        this.difficulty = difficulty;
        this.filepath = filepath;

    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getFilepath() {
        return filepath;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-5s (best time soon)", title, difficulty);
    }
}
