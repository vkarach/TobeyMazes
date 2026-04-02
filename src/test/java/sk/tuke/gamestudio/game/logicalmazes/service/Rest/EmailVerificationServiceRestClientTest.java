package sk.tuke.gamestudio.game.logicalmazes.service.Rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import sk.tuke.gamestudio.service.impl.Rest.EmailVerificationServiceRestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class EmailVerificationServiceRestClientTest extends BaseRestClientTest {

    private EmailVerificationServiceRestClient emailService;

    @BeforeEach
    void init() {
        emailService = new EmailVerificationServiceRestClient(restTemplate, restClientConfig);
    }

    @Test
    void getCodeByEmailTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/emails/test@mail.com/code"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("1234", MediaType.APPLICATION_JSON));

        assertEquals(1234, emailService.getCodeByEmail("test@mail.com"));
        mockServer.verify();
    }

    @Test
    void saveEmailVerificationCodeTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/emails/test@mail.com/code/1234"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> emailService.saveEmailVerificationCode("test@mail.com", 1234));
        mockServer.verify();
    }

    @Test
    void expireEmailTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/emails/test@mail.com/code"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> emailService.expireEmail("test@mail.com"));
        mockServer.verify();
    }
}
