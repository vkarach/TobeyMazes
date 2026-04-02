package sk.tuke.gamestudio.service.impl.JPA;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.EmailVerification;
import sk.tuke.gamestudio.repository.EmailVerificationRepository;
import sk.tuke.gamestudio.service.EmailVerificationService;

import java.time.LocalDateTime;
import java.util.Optional;

@Profile("server")
@Service
public class EmailVerificationServiceJPA implements EmailVerificationService {
    private final EmailVerificationRepository emailVerificationRepository;

    public EmailVerificationServiceJPA(EmailVerificationRepository emailVerificationRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
    }

    @Override
    public Integer getCodeByEmail(String email) {
        Optional<EmailVerification> emailEntity =
                emailVerificationRepository.findFirstByEmailAndExpireAtAfter(email, LocalDateTime.now());
        return  emailEntity.map(EmailVerification::getCode).orElse(null);
    }

    @Override
    public void saveEmailVerificationCode(String email, int code) {
        EmailVerification emailVerificationEntity = new EmailVerification(email, code);
        emailVerificationEntity.setExpireAt(LocalDateTime.now().plusMinutes(10));
        emailVerificationRepository.save(emailVerificationEntity);
    }

    @Override
    public void expireEmail(String email) {
        emailVerificationRepository.expireEmail(email);
    }
}
