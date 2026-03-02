package sk.tuke.gamestudio.game.logicalmazes;

import sk.tuke.gamestudio.service.UserServiceJDBC;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;


public class UserServiceTest {
    private final UserServiceJDBC userService;

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

        int id = userService.getUserIdByUserName(userName);

        assertEquals(userId, id);

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
        System.out.println("generateSessionTest: got token from generateSession: " + sessionToken);
        int id = userService.getUserIdBySessionToken(sessionToken);

        assertEquals(userId, id);

        userService.deleteUserByName(userName); // cleanup
    }
}
