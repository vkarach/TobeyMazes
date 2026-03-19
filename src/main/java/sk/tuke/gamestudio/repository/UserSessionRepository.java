package sk.tuke.gamestudio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.tuke.gamestudio.entity.UserSession;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Integer> {
    Optional<UserSession> findBySessionToken(String sessionToken);
    Optional<UserSession> findByUserId(int userId);
}
