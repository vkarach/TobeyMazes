package sk.tuke.gamestudio.service.impl.Rest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.config.RestClientConfig;
import sk.tuke.gamestudio.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Profile({"console", "fxgl"})
@Service
public class UserServiceRestClient implements UserService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserServiceRestClient(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.baseUrl = restClientConfig.getBaseUrl() + "/api/users";
    }

    @Override
    public boolean userExists(String userName) {
        String url = String.format(
                "%s/name/%s/exists", baseUrl, userName
        );
        Boolean exist = restTemplate.getForObject(url, Boolean.class);
        if (exist == null) {
            return true;
        }
        return exist;
    }

    @Override
    public boolean emailExists(String email) {
        String url = String.format(
                "%s/email/%s/exists", baseUrl, email
        );
        Boolean exist = restTemplate.getForObject(url, Boolean.class);
        if (exist == null) {
            return true;
        }
        return exist;
    }

    @Override
    public int createUser(String userName, String password, String email) {
        String url = String.format(
                "%s/create", baseUrl
        );
        Map<String, String> body = new HashMap<>();
        body.put("userName", userName);
        body.put("password", password);
        body.put("email", email);

        Integer id = restTemplate.postForObject(url, body, Integer.class);
        if (id == null) {
            throw new RuntimeException("User creation failed");
        }
        return id;
    }

    @Override
    public void deleteUserByName(String userName) {
        String url = String.format(
                "%s/%s", baseUrl, userName
        );
        restTemplate.delete(url);
    }

    @Override
    public Integer getUserIdByName(String userName) {
        String url = String.format(
                "%s/%s/id", baseUrl, userName
        );
        return restTemplate.getForObject(url, Integer.class);
    }

    @Override
    public String getUserNameById(int userId) {
        String url = String.format(
                "%s/%d/name", baseUrl, userId
        );
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public String getPasswordByUserId(int userId) {
        String url = String.format(
                "%s/%d/password", baseUrl, userId
        );
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public String getEmailByUserId(int userId) {
        String url = String.format(
                "%s/%d/email", baseUrl, userId
        );
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public void changePassword(int userId, String newPassword) {
        String url = String.format(
                "%s/%d/password/change", baseUrl, userId
        );
        Map<String, String> body = new HashMap<>();
        body.put("newPassword", newPassword);
        restTemplate.postForObject(url, body, Void.class);
    }
}
