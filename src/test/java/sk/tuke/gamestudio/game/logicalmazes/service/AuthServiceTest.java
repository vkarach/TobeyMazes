package sk.tuke.gamestudio.game.logicalmazes.service;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.AuthService;
import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.impl.AuthServiceImpl;
import sk.tuke.gamestudio.service.impl.UserServiceJDBC;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private final AuthService authService;
    private final UserService userService;

    public AuthServiceTest() {
        this.userService = new UserServiceJDBC();
        this.authService = new AuthServiceImpl(userService);
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

        userService.deleteUserByName(userName);
    }

    @Test
    public void updateSessionTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password);

        String sessionToken = userService.generateSession(userId);

        authService.saveSession(sessionToken);

        User user = authService.getUserBySessionToken();

        assertNull(user);

        authService.updateSession(userId);

        user = authService.getUserBySessionToken();

        assertEquals(userId, user.id());
        assertEquals(userName, user.name());

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
    }

}
