package sk.tuke.gamestudio.game.logicalmazes.ui;

public enum MenuOption {
    START("Start game"),
    PROFILE("Profile"),
    LEADERBOARD("Leaderboard"),
    RATE("Rate game"),
    ABOUT("About"),
    EXIT("Exit");

    private final String title;

    MenuOption(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}