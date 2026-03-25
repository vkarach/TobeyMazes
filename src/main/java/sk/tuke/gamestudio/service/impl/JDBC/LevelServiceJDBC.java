package sk.tuke.gamestudio.service.impl.JDBC;

import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.service.LevelService;
import sk.tuke.gamestudio.service.exception.ScoreException;

import java.sql.*;

public class LevelServiceJDBC implements LevelService {
    public static final String ADD_OR_UPDATE_LEVEL =
            "INSERT INTO levels (level_id, level_name) " +
            "VALUES (?, ?) " +
            "ON CONFLICT (level_id) DO UPDATE SET level_name = EXCLUDED.level_name";

    @Override
    public void addOrUpdateLevel(int levelId, String levelName) {
        try (Connection connection = ConnectionManager.getConnection();
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
            addOrUpdateLevel(level.getId(), level.getTitle());
        }
    }
}
