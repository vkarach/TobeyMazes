package sk.tuke.gamestudio.game.logicalmazes.service.JPA;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.exception.SessionException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SessionServiceJPATest extends BaseJPATest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    private int createUser() {
        return userService.createUser(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID() + "@test.com"
        );
    }

    @Test
    public void createSessionTest() {
        int userId = createUser();

        String token = sessionService.createSession(userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void getUserIdBySessionTokenTest() {
        int userId = createUser();
        String token = sessionService.createSession(userId);

        assertEquals(userId, sessionService.getUserIdBySessionToken(token));
    }

    @Test
    public void getSessionTokenByUserIdTest() {
        int userId = createUser();
        String token = sessionService.createSession(userId);

        assertEquals(token, sessionService.getSessionTokenByUserId(userId));
    }

    @Test
    public void sessionNotExpiredTest() {
        int userId = createUser();
        String token = sessionService.createSession(userId);

        assertFalse(sessionService.sessionTokenExpired(token));
    }

    @Test
    public void updateSessionExpireDateTest() {
        int userId = createUser();
        String token = sessionService.createSession(userId);

        assertDoesNotThrow(() -> sessionService.updateSessionTokenExpireDate(token));
        assertFalse(sessionService.sessionTokenExpired(token));
    }

    @Test
    public void invalidTokenThrowsExceptionTest() {
        assertThrows(SessionException.class,
            () -> sessionService.getUserIdBySessionToken("invalid-token"));
    }
}
