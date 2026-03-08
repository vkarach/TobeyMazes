package sk.tuke.gamestudio.entity;

public class UserScore {
    private final int userId;
    private final String userName;
    private final int totalScore;

    public UserScore(int userId, String userName, int totalScore) {
        this.userId = userId;
        this.userName = userName;
        this.totalScore = totalScore;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public int getTotalScore() {
        return totalScore;
    }
}