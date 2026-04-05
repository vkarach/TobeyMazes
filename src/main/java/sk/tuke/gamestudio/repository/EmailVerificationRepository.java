package sk.tuke.gamestudio.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sk.tuke.gamestudio.entity.EmailVerification;

import java.time.LocalDateTime;
import java.util.Optional;


public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
    Optional<EmailVerification> findFirstByEmailAndExpireAtAfter(String email, LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE EmailVerification e SET e.expireAt = CURRENT_TIMESTAMP WHERE e.email = :email")
    void expireEmail(@Param("email") String email);
}
