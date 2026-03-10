package sk.tuke.gamestudio.entity;

public class Review {
    private final int userId;
    private final int rating;
    private final String comment;

    public Review(int userId, int rating, String comment) {
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    public int getUserId() {
        return userId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}

