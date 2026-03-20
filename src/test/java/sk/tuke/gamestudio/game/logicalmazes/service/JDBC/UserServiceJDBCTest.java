package sk.tuke.gamestudio.game.logicalmazes.service.JDBC;

import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.impl.JDBC.SessionServiceJDBC;
import sk.tuke.gamestudio.service.impl.JDBC.UserServiceJDBC;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;


public class UserServiceJDBCTest {
    private final UserService userService;
    private final SessionService sessionService;

    public UserServiceJDBCTest() {
        this.userService = new UserServiceJDBC();
        this.sessionService = new SessionServiceJDBC();
    }

    @Test
    public void deleteUserFromDBTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        userService.deleteUserByName(userName);

        assertFalse(userService.userExists(userName));
    }


    @Test
    public void addUserToDBTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        assertFalse(userService.userExists(userName));

        userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        userService.deleteUserByName(userName); // cleanup
    }

    @Test
    public void getUserIdByNameTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        Integer id = userService.getUserIdByName(userName);

        assertEquals(userId, id);

        userService.deleteUserByName(userName); // cleanup
    }

    @Test
    public void getPasswordByUserIdTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        String passwordFromDb = userService.getPasswordByUserId(userId);

        assertEquals(password, passwordFromDb);

        userService.deleteUserByName(userName); // cleanup
    }

    @Test
    public void userExistsTest() {
        String userName = "UserWIthSomeLowerAnDUpperCase";
        String password = UUID.randomUUID().toString();

        userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName.toLowerCase()));

        userService.deleteUserByName(userName); // cleanup
    }

    @Test
    public void getUserNameByIdTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        String name = userService.getUserNameById(userId);

        assertEquals(userName, name);

        userService.deleteUserByName(userName); // cleanup
    }

    @Test
    public void createSessionTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        String sessionToken = sessionService.createSession(userId);
        int id = sessionService.getUserIdBySessionToken(sessionToken);

        assertEquals(userId, id);

        userService.deleteUserByName(userName); // cleanup
    }

    // todo: test getUserIdBySessionToken, getSessionTokenByUserId, updateSessionTokenExpireDate
}
