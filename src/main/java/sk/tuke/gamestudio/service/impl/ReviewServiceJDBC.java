package sk.tuke.gamestudio.service.impl;

import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.ReviewService;
import sk.tuke.gamestudio.service.exception.BestResultException;

import java.sql.*;

public class ReviewServiceJDBC implements ReviewService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "as2368";

    public static final String INSERT_REVIEW =
            "INSERT INTO reviews (user_id, rating, comment) VALUES (?, ?, ?)" +
            "ON CONFLICT (user_id) " +
            "DO UPDATE SET rating = EXCLUDED.rating, comment = EXCLUDED.comment";

    public static final String GET_REVIEW =
            "SELECT rating, comment FROM reviews WHERE user_id = ?";

    public static final String GET_OVERALL_RATING =
            "SELECT AVG(rating) AS average_rating FROM reviews";

    @Override
    public void addOrUpdateReview(Review review) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(INSERT_REVIEW)
        ) {
            statement.setInt(1, review.userId());
            statement.setInt(2, review.rating());
            statement.setString(3, review.comment());
            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new BestResultException("Problem adding or updating review", e);
        }
    }

    @Override
    public Review getReview(int userId) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(GET_REVIEW)
        ) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Review(
                            userId,
                            rs.getInt("rating"),
                            rs.getString("comment")
                    );
                }
                return null;
            }
        }
        catch (SQLException e) {
            throw new BestResultException("Problem getting review", e);
        }
    }

    @Override
    public float getOverallRating() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(GET_OVERALL_RATING)
        ) {
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("average_rating");
                }
                return 0;
            }
        }
        catch (SQLException e) {
            throw new BestResultException("Problem getting review", e);
        }
    }
}
