package sk.tuke.gamestudio.server.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.EmailSendService;
import sk.tuke.gamestudio.service.EmailVerificationService;
import sk.tuke.gamestudio.service.UserService;

import java.security.SecureRandom;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final WebGameSession session;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final EmailSendService emailSendService;

    public ProfileController(
            WebGameSession session,
            UserService userService,
            EmailVerificationService emailVerificationService,
            EmailSendService emailSendService
    ) {
        this.session = session;
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.emailSendService = emailSendService;
    }

    @GetMapping
    public String profile(Model model) {
        model.addAttribute("user", session.getCurrentUser());
        return "profile";
    }

    @PostMapping("/register")
    public String register(
        Model model,
        @RequestParam String username,
        @RequestParam String password,
        @RequestParam String email
    ) {
        if (userService.userExists(username)) {
            model.addAttribute("error", "Name already taken");
            return "profile";
        }
        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email already taken");
            return "profile";
        }

        int code = generateCode();
        emailVerificationService.saveEmailVerificationCode(email, code);
        emailSendService.sendCode(email, code);

        model.addAttribute("username", username);
        model.addAttribute("password", password);
        model.addAttribute("email", email);
        return "confirm";
    }

    @PostMapping("/login")
    public String login(
        Model model,
        @RequestParam String username,
        @RequestParam String password
    ) {
        Integer userId = userService.getUserIdByName(username);
        if (userId == null) {
            model.addAttribute("error", "User with this name not found");
            return "profile";
        }
        String hash = userService.getPasswordByUserId(userId);
        if (!BCrypt.checkpw(password, hash)) {
            model.addAttribute("error", "Wrong password");
            return "profile";
        }
        session.setCurrentUser(new User(userId, username));
        return "redirect:/profile";
    }

    @PostMapping("/logout")
    public String logout() {
        session.setCurrentUser(null);
        return "redirect:/profile";
    }

    @PostMapping("/confirm")
    public String confirmCode(
            Model model,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam int userCode
    ) {
        Integer code = emailVerificationService.getCodeByEmail(email);
        if (code == null || code != userCode) {
            model.addAttribute("error", "Wrong code");
            model.addAttribute("username", username);
            model.addAttribute("password", password);
            model.addAttribute("email", email);
            return "confirm";
        }
        emailVerificationService.expireEmail(email);

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        int userId = userService.createUser(username, hash, email);
        session.setCurrentUser(new User(userId, username));

        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        if (session.getCurrentUser() == null) {
            return "redirect:/profile";
        }
        int userId = session.getCurrentUser().getId();
        String email = userService.getEmailByUserId(userId);

        int code = generateCode();
        emailVerificationService.saveEmailVerificationCode(email, code);
        emailSendService.sendCode(email, code);

        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Model model,
            @RequestParam int userCode,
            @RequestParam String newPassword
    ) {
        User user = session.getCurrentUser();
        if (user == null) {
            return "redirect:/profile";
        }
        String email = userService.getEmailByUserId(user.getId());
        Integer code = emailVerificationService.getCodeByEmail(email);

        if (code == null || code != userCode) {
            model.addAttribute("error", "Wrong code");
            return "change-password";
        }
        emailVerificationService.expireEmail(email);
        userService.changePassword(user.getId(), BCrypt.hashpw(newPassword, BCrypt.gensalt()));

        return "redirect:/profile";
    }

    private int generateCode() {
        SecureRandom random = new SecureRandom();
        return 100000 + random.nextInt(900000);
    }
}