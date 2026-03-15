package sk.tuke.gamestudio.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.SecureRandom;

public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final EmailSendService emailSendService;
    private final EmailVerificationService emailVerificationService;
    private final SessionService sessionService;

    public AuthServiceImpl (UserService userService, SessionService sessionService, EmailSendService emailSendService, EmailVerificationService emailVerificationService) {
        this.userService = userService;
        this.emailSendService = emailSendService;
        this.emailVerificationService = emailVerificationService;
        this.sessionService = sessionService;
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

            if (sessionService.sessionTokenExpired(token)) {
                return null;
            }

            sessionService.updateSessionTokenExpireDate(token);

            int userId = sessionService.getUserIdBySessionToken(token);
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
        String sessionToken = sessionService.getSessionTokenByUserId(userId);
        if (sessionToken == null) {
            sessionToken = sessionService.createSession(userId);
        }
        sessionService.updateSessionTokenExpireDate(sessionToken);
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

    public int getCode(int userId) {
        Integer code;
        code = emailVerificationService.getEmailCodeByUserId(userId);
        if (code == null) {
            SecureRandom random = new SecureRandom();
            code = 100000 + random.nextInt(900000);
            emailSendService.sendCode("val200700@gmail.com", code);
            emailVerificationService.saveEmailVerificationCode(userId, code);
        }
        return code;
    }

    public void changePassword(int userId, String newPassword) {
        userService.changePassword(userId, stringToHash(newPassword));
    }

    private String stringToHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    boolean checkPassword(String inputPassword, String passwordHashFromDB) {
        return BCrypt.checkpw(inputPassword, passwordHashFromDB);
    }
}
