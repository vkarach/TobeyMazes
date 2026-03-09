package sk.tuke.gamestudio.service.impl;

import sk.tuke.gamestudio.service.BestResultService;
import sk.tuke.gamestudio.service.exception.BestResultException;
import sk.tuke.gamestudio.entity.UserScore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BestResultServiceJDBC implements BestResultService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "as2368";

    public static final String UPDATE_BEST_TIME =
            "INSERT INTO best_level_results (user_id, level_id, best_time_ms) " +
            "VALUES (?, ?, ?) " +
            "ON CONFLICT (user_id, level_id) " +
            "DO UPDATE SET best_time_ms = EXCLUDED.best_time_ms";

    public static final String UPDATE_BEST_SCORE =
            "INSERT INTO best_level_results (user_id, level_id, best_score) " +
                    "VALUES (?, ?, ?) " +
                    "ON CONFLICT (user_id, level_id) " +
                    "DO UPDATE SET best_score = EXCLUDED.best_score";

    public static final String GET_BEST_TIME = "SELECT best_time_ms FROM best_level_results WHERE user_id = ? AND level_id = ?";

    public static final String GET_BEST_SCORE = "SELECT best_score FROM best_level_results WHERE user_id = ? AND level_id = ?";

    public static final String GET_TOP_TEN_BY_SCORE =
            "SELECT u.user_id, u.user_name, SUM(br.best_score) AS total_score " +
            "FROM best_level_results br " +
            "JOIN users u ON u.user_id = br.user_id " +
            "GROUP BY u.user_id, u.user_name " +
            "ORDER BY total_score DESC " +
            "LIMIT 10";

    @Override
    public void updateBestTime(int userId, int levelId, int timeMs) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(UPDATE_BEST_TIME)
        ) {
           statement.setInt(1, userId);
           statement.setInt(2, levelId);
           statement.setInt(3, timeMs);
           statement.executeUpdate();
        }
        catch (SQLException e) {
                throw new BestResultException("Problem updating best time", e);
        }
    }

    @Override
    public void updateBestScore(int userId, int levelId, int score) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(UPDATE_BEST_SCORE)
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, levelId);
            statement.setInt(3, score);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new BestResultException("Problem updating best score", e);
        }
    }

    @Override
    public Integer getBestTime(int userId, int levelId) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(GET_BEST_TIME)
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, levelId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("best_time_ms");
                }
                return null;
            }
        }
        catch (SQLException e) {
            throw new BestResultException("Problem getting best time", e);
        }
    }

    public Integer getBestScore(int userId, int levelId) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(GET_BEST_SCORE)
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, levelId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("best_score");
                }
                return null;
            }
        }
        catch (SQLException e) {
            throw new BestResultException("Problem getting best score", e);
        }
    }

    @Override
    public List<UserScore> getTopByScore() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(GET_TOP_TEN_BY_SCORE)
        ) {
            try (ResultSet rs = statement.executeQuery()) {
                List<UserScore> userScores = new ArrayList<>();
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String userName = rs.getString("user_name");
                    int totalScore = rs.getInt("total_score");
                    userScores.add(new UserScore(userId, userName, totalScore));
                }
                return userScores;
            }
        }
        catch (SQLException e) {
            throw new BestResultException("Problem getting best players id", e);
        }
    }
}
