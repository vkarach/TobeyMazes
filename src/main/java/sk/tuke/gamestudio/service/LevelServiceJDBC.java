package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.game.logicalmazes.core.Level;

import java.sql.*;

public class LevelServiceJDBC implements LevelService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "as2368";

    public static final String ADD_OR_UPDATE_LEVEL =
            "INSERT INTO levels (level_id, level_name) " +
            "VALUES (?, ?) " +
            "ON CONFLICT (level_id) DO UPDATE SET level_name = EXCLUDED.level_name";

    @Override
    public void addAndUpdateLevel(int levelId, String levelName) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(ADD_OR_UPDATE_LEVEL)
        ) {
            statement.setInt(1, levelId);
            statement.setString(2, levelName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ScoreException("Problem inserting level", e);
        }
    }

    @Override
    public void syncLevelsFromEnum(Class<Level> levelEnum) {
        for (Level level : levelEnum.getEnumConstants()) {
            addAndUpdateLevel(level.getId(), level.getTitle());
        }
    }
}
