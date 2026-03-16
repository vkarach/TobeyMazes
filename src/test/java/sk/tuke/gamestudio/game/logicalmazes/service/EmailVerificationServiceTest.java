package sk.tuke.gamestudio.game.logicalmazes.service;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.service.EmailVerificationService;
import sk.tuke.gamestudio.service.impl.EmailVerificationServiceJDBC;
import static org.junit.jupiter.api.Assertions.*;

public class EmailVerificationServiceTest {
    EmailVerificationService emailVerificationService;

    public EmailVerificationServiceTest() {
        this.emailVerificationService = new EmailVerificationServiceJDBC();
    }

    @Test
    public void saveAndGetEmailVerificationCodeTest() {
        String testEmail = "testEmail@gmail.com";
        int code = 123456;
        emailVerificationService.saveEmailVerificationCode(testEmail, code);

        int codeFromDb = emailVerificationService.getCodeByEmail(testEmail);

        assertEquals(code, codeFromDb);

        emailVerificationService.expireEmail(testEmail);
    }

    @Test
    public void expireEmailTest() {
        String testEmail = "testEmail@gmail.com";
        int code = 123456;
        emailVerificationService.saveEmailVerificationCode(testEmail, code);

        int codeFromDb = emailVerificationService.getCodeByEmail(testEmail);

        assertEquals(code, codeFromDb);

        emailVerificationService.expireEmail(testEmail);

        assertNull(emailVerificationService.getCodeByEmail(testEmail));
    }
}
