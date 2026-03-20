package sk.tuke.gamestudio.game.logicalmazes.service.JPA;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tuke.gamestudio.service.EmailVerificationService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EmailVerificationServiceJPATest extends BaseJPATest {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    public void saveAndGetCodeTest() {
        String email = UUID.randomUUID() + "@test.com";
        int code = 123456;

        emailVerificationService.saveEmailVerificationCode(email, code);

        Integer retrieved = emailVerificationService.getCodeByEmail(email);
        assertNotNull(retrieved);
        assertEquals(code, retrieved);
    }

    @Test
    public void expireEmailTest() {
        String email = UUID.randomUUID() + "@test.com";

        emailVerificationService.saveEmailVerificationCode(email, 654321);
        assertNotNull(emailVerificationService.getCodeByEmail(email));

        emailVerificationService.expireEmail(email);

        assertNull(emailVerificationService.getCodeByEmail(email));
    }

    @Test
    public void getNullForNonExistentEmailTest() {
        String email = UUID.randomUUID() + "@test.com";

        assertNull(emailVerificationService.getCodeByEmail(email));
    }

    @Test
    public void saveOverwritesOldCodeTest() {
        String email = UUID.randomUUID() + "@test.com";

        emailVerificationService.saveEmailVerificationCode(email, 111111);
        emailVerificationService.saveEmailVerificationCode(email, 222222);

        Integer code = emailVerificationService.getCodeByEmail(email);
        assertNotNull(code);
    }
}
