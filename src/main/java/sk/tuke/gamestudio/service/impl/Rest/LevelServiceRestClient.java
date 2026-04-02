package sk.tuke.gamestudio.service.impl.Rest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.config.RestClientConfig;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.service.LevelService;

@Profile({"console", "fxgl"})
@Service
public class LevelServiceRestClient implements LevelService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LevelServiceRestClient(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.baseUrl = restClientConfig.getBaseUrl() + "/api/levels";
    }


    @Override
    public void addOrUpdateLevel(int levelId, String levelName) {
        String url = String.format(
                "%s/%d?levelName=%s",
                baseUrl, levelId, levelName
        );
        restTemplate.put(url, null);
    }

    @Override
    public void syncLevelsFromEnum(Class<Level> levelEnum) {
        for (Level level : levelEnum.getEnumConstants()) {
            addOrUpdateLevel(level.getId(), level.getTitle());
        }
    }
}
