package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.service.exception.LevelException;

public interface LevelService {
    void addAndUpdateLevel(int levelId, String levelName) throws LevelException;
    void syncLevelsFromEnum(Class<Level> levelEnum) throws LevelException;
}
