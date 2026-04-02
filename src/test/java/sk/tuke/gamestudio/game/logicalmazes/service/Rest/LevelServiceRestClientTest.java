package sk.tuke.gamestudio.game.logicalmazes.service.Rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import sk.tuke.gamestudio.service.impl.Rest.LevelServiceRestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class LevelServiceRestClientTest extends BaseRestClientTest {

    private LevelServiceRestClient levelService;

    @BeforeEach
    void init() {
        levelService = new LevelServiceRestClient(restTemplate, restClientConfig);
    }

    @Test
    void addOrUpdateLevelTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/levels/1?levelName=Easy"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> levelService.addOrUpdateLevel(1, "Easy"));
        mockServer.verify();
    }
}
