package sk.tuke.gamestudio.game.logicalmazes.service.Rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import sk.tuke.gamestudio.service.impl.Rest.BestResultServiceRestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class BestResultServiceRestClientTest extends BaseRestClientTest {

    private BestResultServiceRestClient bestResultService;

    @BeforeEach
    void init() {
        bestResultService = new BestResultServiceRestClient(restTemplate, restClientConfig);
    }

    @Test
    void updateBestTimeTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/1/levels/2/best-time?timeMs=5000"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> bestResultService.updateBestTime(1, 2, 5000L));
        mockServer.verify();
    }

    @Test
    void updateBestScoreTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/1/levels/2/best-score?bestScore=100"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> bestResultService.updateBestScore(1, 2, 100));
        mockServer.verify();
    }

    @Test
    void getBestTimeTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/1/levels/2/best-time"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("5000", MediaType.APPLICATION_JSON));

        assertEquals(5000L, bestResultService.getBestTime(1, 2));
        mockServer.verify();
    }

    @Test
    void getBestScoreTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/1/levels/2/best-score"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("100", MediaType.APPLICATION_JSON));

        assertEquals(100, bestResultService.getBestScore(1, 2));
        mockServer.verify();
    }

    @Test
    void getBestOverallScoreTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/1/overall-score"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("300", MediaType.APPLICATION_JSON));

        assertEquals(300, bestResultService.getBestOverallScore(1));
        mockServer.verify();
    }

    @Test
    void getTopByScoreTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/leaderboard"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"userId\":1,\"score\":300},{\"userId\":2,\"score\":200}]",
                        MediaType.APPLICATION_JSON
                ));

        var results = bestResultService.getTopByScore();
        assertNotNull(results);
        assertEquals(2, results.size());
        mockServer.verify();
    }

    @Test
    void getBestResultsByUserIdTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/1/best-results"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var results = bestResultService.getBestResultsByUserId(1);
        assertNotNull(results);
        mockServer.verify();
    }
}
