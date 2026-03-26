package sk.tuke.gamestudio.service.impl.JPA;

import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.UserSession;
import sk.tuke.gamestudio.repository.UserSessionRepository;
import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.exception.SessionException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionServiceJPA implements SessionService {
    private final UserSessionRepository userSessionRepository;

    public SessionServiceJPA(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    public String createSession(int userId) {
        String token = UUID.randomUUID().toString();

        UserSession session = new UserSession(userId);
        session.setSessionToken(token);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpireAt(LocalDateTime.now().plusMonths(1));
        userSessionRepository.save(session);

        return token;
    }

    @Override
    public int getUserIdBySessionToken(String sessionToken) {
        Optional<UserSession> userSession = userSessionRepository.findBySessionToken(sessionToken);
        if (userSession.isPresent()) {
            return userSession.get().getUserId();
        }
        else {
            throw new SessionException("Session not found");
        }
    }

    @Override
    public void updateSessionTokenExpireDate(String sessionToken) {
        Optional<UserSession> userSession = userSessionRepository.findBySessionToken(sessionToken);
        if (userSession.isPresent()) {
            UserSession session = userSession.get();
            session.setExpireAt(LocalDateTime.now().plusMonths(1));
            userSessionRepository.save(session);
        }
        else {
            throw new SessionException("Session not found");
        }
    }

    @Override
    public String getSessionTokenByUserId(int userId) {
        Optional<UserSession> userSession = userSessionRepository.findByUserId(userId);
        return userSession.map(UserSession::getSessionToken).orElse(null);
    }

    @Override
    public boolean sessionTokenExpired(String sessionToken) {
        Optional<UserSession> userSession = userSessionRepository.findBySessionToken(sessionToken);
        if (userSession.isPresent()) {
            UserSession session = userSession.get();
            return session.getExpireAt().isBefore(LocalDateTime.now());
        }
        else {
            return true;
        }
    }
}
