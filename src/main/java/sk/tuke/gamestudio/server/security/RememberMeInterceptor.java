package sk.tuke.gamestudio.server.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.controller.WebGameSession;
import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.UserService;

@Profile("server")
@Component
public class RememberMeInterceptor implements HandlerInterceptor {

    public static final String COOKIE_NAME = "remember_token";
    public static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 days

    private final WebGameSession webSession;
    private final SessionService sessionService;
    private final UserService userService;

    public RememberMeInterceptor(WebGameSession webSession,
                                  SessionService sessionService,
                                  UserService userService) {
        this.webSession = webSession;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (webSession.getCurrentUser() != null) return true;

        String token = extractToken(request);
        if (token == null) return true;

        try {
            if (sessionService.sessionTokenExpired(token)) {
                clearCookie(response);
                return true;
            }
            Integer userId = sessionService.getUserIdBySessionToken(token);
            String userName = userService.getUserNameById(userId);
            if (userName == null) {
                clearCookie(response);
                return true;
            }
            webSession.setCurrentUser(new User(userId, userName));
            sessionService.updateSessionTokenExpireDate(token);
        } catch (Exception e) {
            clearCookie(response);
        }
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (COOKIE_NAME.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    public static void setCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    public static void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
