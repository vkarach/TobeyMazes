package sk.tuke.gamestudio.game.logicalmazes.service.Rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import sk.tuke.gamestudio.service.impl.Rest.UserServiceRestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class UserServiceRestClientTest extends BaseRestClientTest {

    private UserServiceRestClient userService;

    @BeforeEach
    void init() {
        userService = new UserServiceRestClient(restTemplate, restClientConfig);
    }

    @Test
    void userExistsTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/name/john/exists"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        assertTrue(userService.userExists("john"));
        mockServer.verify();
    }

    @Test
    void userNotExistsTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/name/unknown/exists"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("false", MediaType.APPLICATION_JSON));

        assertFalse(userService.userExists("unknown"));
        mockServer.verify();
    }

    @Test
    void emailExistsTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/email/test@mail.com/exists"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        assertTrue(userService.emailExists("test@mail.com"));
        mockServer.verify();
    }

    @Test
    void createUserTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/create"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("42", MediaType.APPLICATION_JSON));

        int id = userService.createUser("john", "pass", "j@mail.com");
        assertEquals(42, id);
        mockServer.verify();
    }

    @Test
    void deleteUserByNameTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/john"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> userService.deleteUserByName("john"));
        mockServer.verify();
    }

    @Test
    void getUserIdByNameTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/john/id"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("5", MediaType.APPLICATION_JSON));

        assertEquals(5, userService.getUserIdByName("john"));
        mockServer.verify();
    }

    @Test
    void getUserNameByIdTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/5/name"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("john", MediaType.TEXT_PLAIN));

        assertEquals("john", userService.getUserNameById(5));
        mockServer.verify();
    }

    @Test
    void getPasswordByUserIdTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/5/password"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("secret", MediaType.TEXT_PLAIN));

        assertEquals("secret", userService.getPasswordByUserId(5));
        mockServer.verify();
    }

    @Test
    void getEmailByUserIdTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/5/email"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("j@mail.com", MediaType.TEXT_PLAIN));

        assertEquals("j@mail.com", userService.getEmailByUserId(5));
        mockServer.verify();
    }

    @Test
    void changePasswordTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/5/password/change"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> userService.changePassword(5, "newPass"));
        mockServer.verify();
    }
}
