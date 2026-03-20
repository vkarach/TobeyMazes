package sk.tuke.gamestudio.game.logicalmazes.service.JPA;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tuke.gamestudio.service.UserService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceJPATest extends BaseJPATest {

    @Autowired
    private UserService userService;

    @Test
    public void createUserTest() {
        String name = UUID.randomUUID().toString();
        String email = UUID.randomUUID() + "@test.com";

        int userId = userService.createUser(name, "password", email);

        assertTrue(userId > 0);
        assertTrue(userService.userExists(name));
        assertTrue(userService.emailExists(email));
    }

    @Test
    public void getUserIdByNameTest() {
        String name = UUID.randomUUID().toString();
        int userId = userService.createUser(name, "password", UUID.randomUUID() + "@test.com");

        Integer found = userService.getUserIdByName(name);
        assertNotNull(found);
        assertEquals(userId, found);
    }

    @Test
    public void getUserNameByIdTest() {
        String name = UUID.randomUUID().toString();
        int userId = userService.createUser(name, "password", UUID.randomUUID() + "@test.com");

        assertEquals(name, userService.getUserNameById(userId));
    }

    @Test
    public void deleteUserByNameTest() {
        String name = UUID.randomUUID().toString();
        userService.createUser(name, "password", UUID.randomUUID() + "@test.com");

        assertTrue(userService.userExists(name));
        userService.deleteUserByName(name);
        assertFalse(userService.userExists(name));
    }

    @Test
    public void changePasswordTest() {
        String name = UUID.randomUUID().toString();
        int userId = userService.createUser(name, "oldPassword", UUID.randomUUID() + "@test.com");

        userService.changePassword(userId, "newPassword");

        assertEquals("newPassword", userService.getPasswordByUserId(userId));
    }

    @Test
    public void getEmailByUserIdTest() {
        String email = UUID.randomUUID() + "@test.com";
        int userId = userService.createUser(UUID.randomUUID().toString(), "password", email);

        assertEquals(email, userService.getEmailByUserId(userId));
    }

    @Test
    public void userNotExistsTest() {
        assertFalse(userService.userExists("nonexistent_" + UUID.randomUUID()));
        assertNull(userService.getUserIdByName("nonexistent_" + UUID.randomUUID()));
    }
}
