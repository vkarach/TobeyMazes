package sk.tuke.gamestudio.game.logicalmazes.service.Rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import sk.tuke.gamestudio.service.impl.Rest.SessionServiceRestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class SessionServiceRestClientTest extends BaseRestClientTest {

    private SessionServiceRestClient sessionService;

    @BeforeEach
    void init() {
        sessionService = new SessionServiceRestClient(restTemplate, restClientConfig);
    }

    @Test
    void createSessionTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/sessions/1/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("abc-token", MediaType.TEXT_PLAIN));

        assertEquals("abc-token", sessionService.createSession(1));
        mockServer.verify();
    }

    @Test
    void getUserIdBySessionTokenTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/sessions/abc-token/user-id"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        assertEquals(1, sessionService.getUserIdBySessionToken("abc-token"));
        mockServer.verify();
    }

    @Test
    void updateSessionTokenExpireDateTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/sessions/abc-token/expire"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> sessionService.updateSessionTokenExpireDate("abc-token"));
        mockServer.verify();
    }

    @Test
    void getSessionTokenByUserIdTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/sessions/1/token"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("abc-token", MediaType.TEXT_PLAIN));

        assertEquals("abc-token", sessionService.getSessionTokenByUserId(1));
        mockServer.verify();
    }

    @Test
    void sessionTokenExpiredTrueTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/sessions/abc-token/expired"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        assertTrue(sessionService.sessionTokenExpired("abc-token"));
        mockServer.verify();
    }

    @Test
    void sessionTokenExpiredFalseTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/sessions/abc-token/expired"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("false", MediaType.APPLICATION_JSON));

        assertFalse(sessionService.sessionTokenExpired("abc-token"));
        mockServer.verify();
    }
}
