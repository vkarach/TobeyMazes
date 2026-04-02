package sk.tuke.gamestudio.service.impl.Rest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.config.RestClientConfig;
import sk.tuke.gamestudio.service.EmailVerificationService;
import sk.tuke.gamestudio.service.exception.EmailException;

@Profile({"console", "fxgl"})
@Service
public class EmailVerificationServiceRestClient implements EmailVerificationService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public EmailVerificationServiceRestClient(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.baseUrl = restClientConfig.getBaseUrl() + "/api/emails";
    }

    @Override
    public Integer getCodeByEmail(String email) {
        String url = String.format(
                "%s/%s/code",
                baseUrl, email
        );
        return restTemplate.getForObject(url, Integer.class);
    }

    @Override
    public void saveEmailVerificationCode(String email, int code) {
        String url = String.format(
                "%s/%s/code/%d",
                baseUrl, email, code
        );
        restTemplate.put(url, null);
    }

    @Override
    public void expireEmail(String email) throws EmailException {
        String url = String.format(
                "%s/%s/code",
                baseUrl, email
        );
        restTemplate.delete(url);
    }
}
