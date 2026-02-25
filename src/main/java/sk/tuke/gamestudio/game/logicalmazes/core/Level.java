package sk.tuke.gamestudio.game.logicalmazes.core;

public enum Level {
    INTRODUCTION("Introduction", Difficulty.EASY, "map_1.txt");

    Level(String title, Difficulty difficulty, String filename) {
        this.title = title;
        this.difficulty = difficulty;
        this.filename = filename;
    }

    public enum Difficulty { // maybe in Difficulty.class
        EASY, MEDIUM, HARD
    }

    private final String title;
    private final Difficulty difficulty;
    private final String filename;
//    private long record;

    public String getTitle() {
        return title;
    }
    public Difficulty getDifficulty() {
        return difficulty;
    }
    public String getFilename() {
        return filename;
    }
}
