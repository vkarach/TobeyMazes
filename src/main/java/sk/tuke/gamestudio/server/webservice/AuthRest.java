package sk.tuke.gamestudio.server.webservice;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.service.AuthService;
import sk.tuke.gamestudio.service.EmailVerificationService;
import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Profile("server")
public class AuthRest {

    private final UserService userService;
    private final SessionService sessionService;
    private final EmailVerificationService emailVerificationService;
    private final AuthService authService;

    public AuthRest(UserService userService,
                    SessionService sessionService,
                    EmailVerificationService emailVerificationService,
                    AuthService authService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.emailVerificationService = emailVerificationService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Integer userId = userService.getUserIdByName(username);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String hash = userService.getPasswordByUserId(userId);
        if (!BCrypt.checkpw(password, hash)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String token = getOrCreateToken(userId);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/send-email-code")
    public void sendEmailCode(@RequestBody Map<String, String> body) {
        authService.initiateEmailVerification(body.get("email"));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email    = body.get("email");
        int    code     = Integer.parseInt(body.get("code"));

        if (userService.userExists(username)) return ResponseEntity.status(HttpStatus.CONFLICT).build();
        if (userService.emailExists(email))   return ResponseEntity.status(HttpStatus.CONFLICT).build();

        Integer storedCode = emailVerificationService.getCodeByEmail(email);
        if (storedCode == null || storedCode != code) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        emailVerificationService.expireEmail(email);

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        int userId = userService.createUser(username, hashedPassword, email);
        String token = sessionService.createSession(userId);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/send-password-change-code")
    public void sendPasswordChangeCode(Authentication auth) {
        int userId = (Integer) auth.getPrincipal();
        authService.initiatePasswordChange(userId);
    }

    @PostMapping("/change-password-with-code")
    public ResponseEntity<Void> changePasswordWithCode(Authentication auth,
                                                       @RequestBody Map<String, String> body) {
        int userId = (Integer) auth.getPrincipal();
        int code = Integer.parseInt(body.get("code"));
        String newPasswordHash = body.get("newPassword"); // already hashed by client

        String email = userService.getEmailByUserId(userId);
        Integer storedCode = emailVerificationService.getCodeByEmail(email);
        if (storedCode == null || storedCode != code) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        emailVerificationService.expireEmail(email);
        userService.changePassword(userId, newPasswordHash);
        return ResponseEntity.ok().build();
    }

    private String getOrCreateToken(int userId) {
        String token = sessionService.getSessionTokenByUserId(userId);
        if (token == null) {
            token = sessionService.createSession(userId);
        } else {
            sessionService.updateSessionTokenExpireDate(token);
        }
        return token;
    }
}
