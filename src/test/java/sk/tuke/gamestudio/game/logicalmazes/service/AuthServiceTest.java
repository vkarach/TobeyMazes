package sk.tuke.gamestudio.game.logicalmazes.service;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.AuthService;
import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.impl.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private final AuthService authService;
    private final UserService userService;
    private final SessionService sessionService;

    public AuthServiceTest() {
        this.userService = new UserServiceJDBC();
        this.sessionService = new SessionServiceJDBC();
        this.authService = new AuthServiceImpl(userService,sessionService, null, null);
    }

    @Test
    public void registerTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        User user = authService.register(userName, password);

        assertTrue(userService.userExists(userName));

        Integer userId = userService.getUserIdByUserName(userName);

        assertEquals(user.id(), userId);

        String name = userService.getUserNameByUserId(userId);

        assertEquals(user.name(), name);

        authService.deleteSession();
        userService.deleteUserByName(userName);
    }

    @Test
    public void loginTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        User registeredUser = authService.register(userName, password);

        assertTrue(userService.userExists(userName));

        User loggedInUser = authService.login(userName, password);

        assertEquals(registeredUser.id(), loggedInUser.id());
        assertEquals(registeredUser.name(), loggedInUser.name());

        User loggedInWithWrongPassword = authService.login(userName, "wrongPassword");

        assertNull(loggedInWithWrongPassword);

        authService.deleteSession();
        userService.deleteUserByName(userName);
    }

    @Test
    public void updateSessionTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password);

        String sessionToken = sessionService.createSession(userId);

        authService.saveSession(sessionToken);

        User user = authService.getUserBySessionToken();

        assertNull(user);

        authService.updateSession(userId);

        user = authService.getUserBySessionToken();

        assertEquals(userId, user.id());
        assertEquals(userName, user.name());

        authService.deleteSession();
        userService.deleteUserByName(userName);
    }

    @Test
    public void getUserBySessionTokenTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password);

        assertTrue(userService.userExists(userName));

        authService.updateSession(userId);

        User user = authService.getUserBySessionToken();

        assertEquals(user.id(), userId);
        assertEquals(user.name(), userName);

        authService.deleteSession();
        userService.deleteUserByName(userName);
    }

    @Test
    public void deleteSessionTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password);

        assertTrue(userService.userExists(userName));

        authService.updateSession(userId);

        User user = authService.getUserBySessionToken();

        assertNotNull(user);

        authService.deleteSession();

        user = authService.getUserBySessionToken();

        assertNull(user);

        userService.deleteUserByName(userName);
    }

    @Test
    public void sessionWithExpiredDateTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password);

        assertTrue(userService.userExists(userName));

        String sessionToken = sessionService.createSession(userId);

        authService.saveSession(sessionToken);

        User user = authService.getUserBySessionToken();

        assertNull(user);

        authService.updateSession(userId);

        User userFromSession = authService.getUserBySessionToken();

        assertEquals(userId, userFromSession.id());
        assertEquals(userName, userFromSession.name());

        authService.deleteSession();
        userService.deleteUserByName(userName);
    }
}
