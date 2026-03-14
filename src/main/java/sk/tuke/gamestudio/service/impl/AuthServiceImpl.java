package sk.tuke.gamestudio.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.AuthService;
import sk.tuke.gamestudio.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthServiceImpl implements AuthService {
    private final UserService userService;

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    public void saveSession(String sessionToken) {
        String baseDir = System.getProperty("user.home");
        Path sessionDir = Paths.get(baseDir, ".logicalmaze");
        try {
            Files.createDirectories(sessionDir);
            Path sessionFile = sessionDir.resolve("session.token");
            Files.writeString(sessionFile, sessionToken);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUserBySessionToken() {
        String baseDir = System.getProperty("user.home");
        Path sessionDir = Paths.get(baseDir, ".logicalmaze");

        try {
            Files.createDirectories(sessionDir);
            Path sessionFile = sessionDir.resolve("session.token");
            String token = Files.readString(sessionFile);

            if (userService.sessionTokenExpired(token)) {
                return null;
            }

            userService.updateSessionTokenExpireDate(token);

            int userId = userService.getUserIdBySessionToken(token);
            String userName = userService.getUserNameByUserId(userId);

            return new User(userId, userName);
        }
        catch (NoSuchFileException e) {
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSession(int userId) {
        String sessionToken = userService.getSessionTokenByUserId(userId);
        if (sessionToken == null) {
            sessionToken = userService.generateSession(userId);
        }
        userService.updateSessionTokenExpireDate(sessionToken);
        saveSession(sessionToken);
    }

    public User register(String name, String password) {
        if (userService.userExists(name)) {
            return null;
        }

        int userId = userService.createUser(name, stringToHash(password));
        updateSession(userId);

        return new User(userId, name);
    }

    public User login(String name, String password) {
        Integer userId = userService.getUserIdByUserName(name);
        if (userId == null || !checkPassword(password, userService.getPasswordByUserId(userId))) {
            return null;
        }

        name = userService.getUserNameByUserId(userId);
        updateSession(userId);

        return new User(userId, name);
    }

    public void deleteSession() {
        String baseDir = System.getProperty("user.home");
        Path sessionDir = Paths.get(baseDir, ".logicalmaze");
        Path sessionFile = sessionDir.resolve("session.token");
        try {
            Files.deleteIfExists(sessionFile);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String stringToHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    boolean checkPassword(String inputPassword, String passwordHashFromDB) {
        return BCrypt.checkpw(inputPassword, passwordHashFromDB);
    }
}
