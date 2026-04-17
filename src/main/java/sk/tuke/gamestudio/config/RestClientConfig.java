package sk.tuke.gamestudio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Profile({"console", "fxgl"})
@Configuration
public class RestClientConfig {
    private static final String BASE_URL = "http://localhost:8080";
//    private static final String BASE_URL = "https://tobeymazes.xyz/";

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String token = readSessionToken();
            if (token != null) {
                request.getHeaders().setBearerAuth(token);
            }
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    private String readSessionToken() {
        Path tokenFile = Paths.get(System.getProperty("user.home"), ".logicalmaze", "session.token");
        try {
            return Files.readString(tokenFile).strip();
        }
        catch (IOException e) {
            return null;
        }
    }
}
