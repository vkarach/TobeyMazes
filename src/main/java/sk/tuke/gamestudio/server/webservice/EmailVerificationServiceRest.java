package sk.tuke.gamestudio.server.webservice;

import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.service.EmailVerificationService;


@RestController
@RequestMapping("/api/emails")
public class EmailVerificationServiceRest {
    private final EmailVerificationService emailVerificationService;

    public EmailVerificationServiceRest(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/{email}/code")
    public Integer getCodeByEmail(@PathVariable String email) {
        return emailVerificationService.getCodeByEmail(email);
    }

    @PutMapping("/{email}/code/{code}")
    public void saveEmailVerificationCode(@PathVariable String email, @PathVariable int code) {
        emailVerificationService.saveEmailVerificationCode(email, code);
    }

    @DeleteMapping("/{email}/code")
    public void expireEmail(@PathVariable String email) {
        emailVerificationService.expireEmail(email);
    }
}
