package sk.tuke.gamestudio.service.impl.Rest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.config.RestClientConfig;
import sk.tuke.gamestudio.service.SessionService;

@Profile({"console", "fxgl"})
@Service
public class SessionServiceRestClient implements SessionService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SessionServiceRestClient(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.baseUrl = restClientConfig.getBaseUrl() + "/api/sessions";
    }

    @Override
    public String createSession(int userId) {
        String url = String.format(
                "%s/%d/token", baseUrl, userId
        );
        return restTemplate.postForObject(url, null, String.class);
    }

    @Override
    public Integer getUserIdBySessionToken(String sessionToken) {
        String url = String.format(
                "%s/%s/user-id", baseUrl, sessionToken
        );
        return restTemplate.getForObject(url, Integer.class);
    }

    @Override
    public void updateSessionTokenExpireDate(String sessionToken) {
        String url = String.format(
                "%s/%s/expire", baseUrl, sessionToken
        );
        restTemplate.put(url, null);
    }

    @Override
    public String getSessionTokenByUserId(int userId) {
        String url = String.format(
                "%s/%d/token", baseUrl, userId
        );
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public boolean sessionTokenExpired(String sessionToken) {
        String url = String.format(
                "%s/%s/expired", baseUrl, sessionToken
        );
        Boolean expired = restTemplate.getForObject(url, Boolean.class);
        if (expired == null) {
            return true;
        }
        return expired;
    }
}
