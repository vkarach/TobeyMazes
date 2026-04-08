package sk.tuke.gamestudio.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.config.RestClientConfig;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.security.SecureRandom;

@Component
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final SessionService sessionService;
    private final EmailSendService emailSendService;
    private final EmailVerificationService emailVerificationService;

    // null on server profile - RestTemplate bean exists only on console/fxgl
    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private RestClientConfig restClientConfig;

    public AuthServiceImpl(
            UserService userService,
            SessionService sessionService,
            EmailSendService emailSendService,
            EmailVerificationService emailVerificationService
    ) {
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
            String userName = userService.getUserNameById(userId);

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

    public User register(String name, String password, String email) {
        if (userService.userExists(name)) {
            return null;
        }

        int userId = userService.createUser(name, stringToHash(password), email);
        updateSession(userId);

        return new User(userId, name);
    }

    public User login(String name, String password) {
        if (restTemplate != null) {
            return loginViaServer(name, password);
        }
        Integer userId = userService.getUserIdByName(name);
        if (userId == null || !checkPassword(password, userService.getPasswordByUserId(userId))) {
            return null;
        }
        name = userService.getUserNameById(userId);
        updateSession(userId);
        return new User(userId, name);
    }

    private User loginViaServer(String name, String password) {
        String url = restClientConfig.getBaseUrl() + "/api/auth/login";
        try {
            String token = restTemplate.postForObject(
                    url, Map.of("username", name, "password", password), String.class
            );
            if (token == null) return null;
            saveSession(token);
            int userId = sessionService.getUserIdBySessionToken(token);
            return new User(userId, name);
        }
        catch (HttpClientErrorException.Unauthorized e) {
            return null;
        }
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
    private int generateCode() {
        SecureRandom random = new SecureRandom();
        return 100000 + random.nextInt(900000);
    }

    private int getOrCreateVerificationCodeByEmail(String email) {
        Integer code = emailVerificationService.getCodeByEmail(email);

        if (code != null) {
            return code;
        }

        code = generateCode();
        emailVerificationService.saveEmailVerificationCode(email, code);
        emailSendService.sendCode(email, code);

        return code;
    }

    public void initiateEmailVerification(String email) {
        if (restTemplate != null) {
            restTemplate.postForObject(
                    restClientConfig.getBaseUrl() + "/api/auth/send-email-code",
                    Map.of("email", email), Void.class
            );
        } else {
            getOrCreateVerificationCodeByEmail(email);
        }
    }

    public User registerWithCode(String name, String password, String email, int code) {
        if (restTemplate != null) {
            try {
                String token = restTemplate.postForObject(
                        restClientConfig.getBaseUrl() + "/api/auth/register",
                        Map.of("username", name, "password", password, "email", email, "code", String.valueOf(code)),
                        String.class
                );
                if (token == null) return null;
                saveSession(token);
                int userId = sessionService.getUserIdBySessionToken(token);
                return new User(userId, name);
            } catch (HttpClientErrorException e) {
                return null;
            }
        }
        Integer storedCode = emailVerificationService.getCodeByEmail(email);
        if (storedCode == null || storedCode != code) return null;
        emailVerificationService.expireEmail(email);
        int userId = userService.createUser(name, stringToHash(password), email);
        updateSession(userId);
        return new User(userId, name);
    }

    public void initiatePasswordChange(int userId) {
        if (restTemplate != null) {
            restTemplate.postForObject(
                restClientConfig.getBaseUrl() + "/api/auth/send-password-change-code",
                null, Void.class
            );
        }
        else {
            String email = userService.getEmailByUserId(userId);
            getOrCreateVerificationCodeByEmail(email);
        }
    }

    public boolean changePasswordWithCode(int userId, int code, String newPassword) {
        if (restTemplate != null) {
            try {
                restTemplate.postForObject(
                        restClientConfig.getBaseUrl() + "/api/auth/change-password-with-code",
                        Map.of("code", String.valueOf(code), "newPassword", stringToHash(newPassword)),
                        Void.class
                );
                return true;
            } catch (HttpClientErrorException e) {
                return false;
            }
        }
        String email = userService.getEmailByUserId(userId);
        Integer storedCode = emailVerificationService.getCodeByEmail(email);
        if (storedCode == null || storedCode != code) return false;
        emailVerificationService.expireEmail(email);
        userService.changePassword(userId, stringToHash(newPassword));
        return true;
    }

    // legacy - used only by web controllers (server-mode)

    public int getOrCreateEmailVerificationCode(int userId) {
        String email = userService.getEmailByUserId(userId);
        return getOrCreateVerificationCodeByEmail(email);
    }

    public int sendOrGetVerificationCodeByEmail(String email) {
        return getOrCreateVerificationCodeByEmail(email);
    }

    public void expireEmail(String email) {
        emailVerificationService.expireEmail(email);
    }

    public void expireEmailByUserId(int userId) {
        String email = userService.getEmailByUserId(userId);
        emailVerificationService.expireEmail(email);
    }

    public boolean emailTaken(String email) {
        return userService.emailExists(email);
    }

    public boolean userNameTaken(String userName) {
        return userService.userExists(userName);
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
