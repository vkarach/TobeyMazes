package sk.tuke.gamestudio.server.webservice;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sk.tuke.gamestudio.service.SessionService;

@RestController
@RequestMapping("/api/sessions")
public class SessionServiceRest {
    private final SessionService sessionService;

    public SessionServiceRest(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/{userId}/token")
    public String createSession(@PathVariable int userId, Authentication auth) {
        requireOwnership(auth, userId);
        return sessionService.createSession(userId);
    }

    @GetMapping("/{token}/user-id")
    public int getUserIdBySessionToken(@PathVariable("token") String sessionToken) {
        return sessionService.getUserIdBySessionToken(sessionToken);
    }

    @PutMapping("/{token}/expire")
    public void updateSessionTokenExpireDate(@PathVariable("token") String sessionToken) {
        sessionService.updateSessionTokenExpireDate(sessionToken);
    }

    @GetMapping("/{userId}/token")
    public String getSessionTokenByUserId(@PathVariable int userId, Authentication auth) {
        requireOwnership(auth, userId);
        return sessionService.getSessionTokenByUserId(userId);
    }

    private void requireOwnership(Authentication auth, int targetUserId) {
        int tokenUserId = (Integer) auth.getPrincipal();
        if (tokenUserId != targetUserId) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/{token}/expired")
    public boolean sessionTokenExpired(@PathVariable("token") String sessionToken) {
        return sessionService.sessionTokenExpired(sessionToken);
    }
}
