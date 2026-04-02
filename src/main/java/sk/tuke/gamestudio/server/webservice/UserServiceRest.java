package sk.tuke.gamestudio.server.webservice;


import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserServiceRest {
    private final UserService userService;

    public UserServiceRest(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/name/{name}/exists")
    public boolean userExists(@PathVariable("name") String userName) {
       return userService.userExists(userName);
    }

    @GetMapping("/email/{email}/exists")
    public boolean emailExists(@PathVariable String email) {
       return userService.emailExists(email);
    }

    @PostMapping("/create")
    public int createUser(@RequestBody Map<String, String> body) {
        return userService.createUser(
                body.get("userName"),
                body.get("password"),
                body.get("email")
        );
    }

    @DeleteMapping("/{name}")
    void deleteUserByName(@PathVariable("name") String userName) {
        userService.deleteUserByName(userName);
    }

    @GetMapping("/{name}/id")
    Integer getUserIdByName(@PathVariable("name") String userName) {
        return userService.getUserIdByName(userName);
    }

    @GetMapping("/{userId}/name")
    String getUserNameById(@PathVariable int userId) {
        return userService.getUserNameById(userId);
    }

    @GetMapping("/{userId}/password")
    String getPasswordByUserId(@PathVariable int userId) {
        return userService.getPasswordByUserId(userId);
    }

    @GetMapping("/{userId}/email")
    String getEmailByUserId(@PathVariable int userId) {
        return userService.getEmailByUserId(userId);
    }

    @PostMapping("/{userId}/password/change")
    void changePassword(@PathVariable int userId, @RequestBody Map<String, String> body) {
        userService.changePassword(userId, body.get("newPassword"));
    }
}
