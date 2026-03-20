package sk.tuke.gamestudio.service.impl.JPA;

import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.LevelEntity;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.repository.LevelRepository;
import sk.tuke.gamestudio.service.LevelService;

@Service
public class LevelServiceJPA implements LevelService {
    private final LevelRepository levelRepository;

    public LevelServiceJPA(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    @Override
    public void addOrUpdateLevel(int levelId, String levelName) {
        LevelEntity level = new LevelEntity(levelId, levelName);
        levelRepository.save(level);
    }

    @Override
    public void syncLevelsFromEnum(Class<Level> levelEnum) {
        for (Level level : levelEnum.getEnumConstants()) {
            addOrUpdateLevel(level.getId(), level.getTitle());
        }
    }
}
