package sk.tuke.gamestudio.server.webservice;

import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.service.SessionService;

@RestController
@RequestMapping("/api/sessions")
public class SessionServiceRest {
    private final SessionService sessionService;

    public SessionServiceRest(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/{userId}/token")
    public String createSession(@PathVariable int userId) {
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
    public String getSessionTokenByUserId(@PathVariable int userId) {
        return sessionService.getSessionTokenByUserId(userId);
    }

    @GetMapping("/{token}/expired")
    public boolean sessionTokenExpired(@PathVariable("token") String sessionToken) {
        return sessionService.sessionTokenExpired(sessionToken);
    }
}
