package sk.tuke.gamestudio.game.logicalmazes.ui;

public enum ProfileOption {
    REGISTER("Register"),
    LOGIN("Login"),
    LOGOUT("Logout"),
    CHANGE_PASSWORD("Change password"),
    BACK("Back");

    private final String title;

    ProfileOption(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
