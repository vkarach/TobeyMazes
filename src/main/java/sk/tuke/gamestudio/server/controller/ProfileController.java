package sk.tuke.gamestudio.server.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.BestResultService;
import sk.tuke.gamestudio.service.EmailSendService;
import sk.tuke.gamestudio.service.EmailVerificationService;
import sk.tuke.gamestudio.service.UserService;

import java.security.SecureRandom;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final WebGameSession session;
    private final UserService userService;
    private final BestResultService bestResultService;
    private final EmailVerificationService emailVerificationService;
    private final EmailSendService emailSendService;

    public ProfileController(
            WebGameSession session,
            UserService userService,
            BestResultService bestResultService,
            EmailVerificationService emailVerificationService,
            EmailSendService emailSendService
    ) {
        this.session = session;
        this.userService = userService;
        this.bestResultService = bestResultService;
        this.emailVerificationService = emailVerificationService;
        this.emailSendService = emailSendService;
    }

    @GetMapping
    public String profile(Model model) {
        User user = session.getCurrentUser();
        model.addAttribute("user", user);
        if (user != null) {
            Integer score = bestResultService.getBestOverallScore(user.getId());
            model.addAttribute("overallScore", score != null ? score : 0);
        }
        return "profile";
    }

    // AJAX endpoints
    @PostMapping("/login/ajax")
    @ResponseBody
    public Map<String, Object> loginAjax(@RequestParam String username, @RequestParam String password) {
        Integer userId = userService.getUserIdByName(username);
        if (userId == null) return Map.of("error", "User not found");
        if (!BCrypt.checkpw(password, userService.getPasswordByUserId(userId)))
            return Map.of("error", "Wrong password");
        session.setCurrentUser(new User(userId, username));
        return Map.of("ok", true);
    }

    @PostMapping("/register/ajax")
    @ResponseBody
    public Map<String, Object> registerAjax(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email
    ) {
        if (userService.userExists(username)) return Map.of("error", "Name already taken");
        if (userService.emailExists(email))   return Map.of("error", "Email already taken");
        int code = generateCode();
        emailVerificationService.saveEmailVerificationCode(email, code);
        emailSendService.sendCode(email, code);
        return Map.of("ok", true);
    }

    @PostMapping("/confirm/ajax")
    @ResponseBody
    public Map<String, Object> confirmAjax(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam int userCode
    ) {
        Integer code = emailVerificationService.getCodeByEmail(email);
        if (code == null || code != userCode) return Map.of("error", "Wrong code");
        emailVerificationService.expireEmail(email);
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        int userId = userService.createUser(username, hash, email);
        session.setCurrentUser(new User(userId, username));
        return Map.of("ok", true);
    }

    @PostMapping("/change-password/request")
    @ResponseBody
    public Map<String, Object> changePasswordRequest() {
        User user = session.getCurrentUser();
        if (user == null) return Map.of("error", "Not logged in");
        String email = userService.getEmailByUserId(user.getId());
        if (emailVerificationService.getCodeByEmail(email) == null) {
            int code = generateCode();
            emailVerificationService.saveEmailVerificationCode(email, code);
            emailSendService.sendCode(email, code);
        }
        return Map.of("ok", true);
    }

    @PostMapping("/change-password/ajax")
    @ResponseBody
    public Map<String, Object> changePasswordAjax(@RequestParam int userCode, @RequestParam String newPassword) {
        User user = session.getCurrentUser();
        if (user == null) return Map.of("error", "Not logged in");
        String email = userService.getEmailByUserId(user.getId());
        Integer code = emailVerificationService.getCodeByEmail(email);
        if (code == null || code != userCode) return Map.of("error", "Wrong code");
        emailVerificationService.expireEmail(email);
        userService.changePassword(user.getId(), BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        return Map.of("ok", true);
    }

    // Legacy form endpoints (kept for fallback)
    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout() {
        session.setCurrentUser(null);
        return Map.of("ok", true);
    }

    private int generateCode() {
        SecureRandom random = new SecureRandom();
        return 100000 + random.nextInt(900000);
    }
}
