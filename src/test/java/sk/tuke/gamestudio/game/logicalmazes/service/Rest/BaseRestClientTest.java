package sk.tuke.gamestudio.game.logicalmazes.service.Rest;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.config.RestClientConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseRestClientTest {
    protected static final String BASE_URL = "http://localhost:8081";

    protected RestTemplate restTemplate;
    protected MockRestServiceServer mockServer;
    protected RestClientConfig restClientConfig;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        restClientConfig = mock(RestClientConfig.class);
        when(restClientConfig.getBaseUrl()).thenReturn(BASE_URL);
    }
}
