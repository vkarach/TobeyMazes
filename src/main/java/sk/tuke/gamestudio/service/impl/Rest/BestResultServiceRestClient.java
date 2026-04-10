package sk.tuke.gamestudio.service.impl.Rest;

import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.config.RestClientConfig;
import sk.tuke.gamestudio.entity.BestLevelResult;
import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.List;

@Profile({"console", "fxgl"})
@Service
public class BestResultServiceRestClient implements BestResultService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public BestResultServiceRestClient(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.baseUrl = restClientConfig.getBaseUrl() + "/api/users";
    }

    @Override
    public void updateBestTime(int userId, int levelId, long timeMs) {
        String url = String.format(
                "%s/%d/levels/%d/best-time?timeMs=%d",
                baseUrl, userId, levelId, timeMs
        );
        restTemplate.put(url, null);
    }

    @Override
    public void updateBestScore(int userId, int levelId, int score) {
        String url = String.format(
                "%s/%d/levels/%d/best-score?bestScore=%d",
                baseUrl, userId, levelId, score
        );
        restTemplate.put(url, null);
    }

    @Override
    public Long getBestTime(int userId, int levelId) {
        String url = String.format(
                "%s/%d/levels/%d/best-time", baseUrl, userId, levelId
        );
        return restTemplate.getForObject(url, Long.class);
    }

    @Override
    public Integer getBestScore(int userId, int levelId) {
        String url = String.format(
                "%s/%d/levels/%d/best-score", baseUrl, userId, levelId
        );
        return restTemplate.getForObject(url, Integer.class);
    }

    @Override
    public Integer getBestOverallScore(int userId) {
        String url = String.format(
                "%s/%d/overall-score", baseUrl, userId
        );
        return restTemplate.getForObject(url, Integer.class);
    }

    @Override
    public Integer getUserLeaderboardPosition(int userId) {
        String url = String.format(
                "%s/%d/leaderboard-position", baseUrl, userId
        );
        return restTemplate.getForObject(url, Integer.class);
    }

    @Override
    public List<UserScore> getTopByScore() {
        String url = String.format(
                "%s/leaderboard", baseUrl
        );

        ResponseEntity<List<UserScore>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    @Override
    public List<BestLevelResult> getBestResultsByUserId(int userId) {
        String url = String.format(
                "%s/%d/best-results", baseUrl, userId
        );

        ResponseEntity<List<BestLevelResult>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }
}

