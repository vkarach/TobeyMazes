package sk.tuke.gamestudio.entity;


public class Comment {
    private final int userId;
    private final String comment;

    public Comment(int userId, String comment) {
        this.userId = userId;
        this.comment = comment;
    }

    public int getUserId() {
        return userId;
    }

    public String getComment() {
        return comment;
    }
}
