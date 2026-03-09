package sk.tuke.gamestudio.game.logicalmazes.service;

import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.impl.UserServiceJDBC;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;


public class UserServiceTest {
    private final UserService userService;

    public UserServiceTest() {
        this.userService = new UserServiceJDBC();
    }

    @Test
    public void addUserToDBTest() {
        String userName = UUID.randomUUID().toString();

        assertFalse(userService.userExists(userName));

        userService.createUser(userName);

        assertTrue(userService.userExists(userName));

        userService.deleteUserByName(userName); // cleanup
    }

    @Test
    public void deleteUserFromDBTest() {
        String userName = UUID.randomUUID().toString();

        userService.createUser(userName);

        assertTrue(userService.userExists(userName));

        userService.deleteUserByName(userName);

        assertFalse(userService.userExists(userName));
    }

    @Test
    public void getUserIdByUserName() {
        String userName = UUID.randomUUID().toString();
        int userId = userService.createUser(userName);

        assertTrue(userService.userExists(userName));

        Integer id = userService.getUserIdByUserName(userName);

        assertEquals(userId, id);

        userService.deleteUserByName(userName); // cleanup
    }

    @Test
    public void userExistsTest() {
        String userName = "UserWIthSomeLowerAnDUpperCase";
        userService.createUser(userName);

        assertTrue(userService.userExists(userName.toLowerCase()));

        userService.deleteUserByName(userName); // cleanup
    }

    @Test
    public void getUserNameByUserIdTest() {
        String userName = UUID.randomUUID().toString();
        int userId = userService.createUser(userName);

        assertTrue(userService.userExists(userName));

        String name = userService.getUserNameByUserId(userId);

        assertEquals(userName, name);
    }

    @Test
    public void generateSessionTest() {
        String userName = UUID.randomUUID().toString();
        int userId = userService.createUser(userName);

        assertTrue(userService.userExists(userName));

        String sessionToken = userService.generateSession(userId);
        int id = userService.getUserIdBySessionToken(sessionToken);

        assertEquals(userId, id);

        userService.deleteUserByName(userName); // cleanup
    }

    // todo: test getUserIdBySessionToken, getSessionTokenByUserId, updateSessionTokenExpireDate
}
