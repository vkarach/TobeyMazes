package sk.tuke.gamestudio.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.exception.SessionException;

import java.io.IOException;
import java.util.List;

@Component
@Profile("server")
public class ApiTokenAuthFilter extends OncePerRequestFilter {

    private final SessionService sessionService;

    public ApiTokenAuthFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (!sessionService.sessionTokenExpired(token)) {
                    int userId = sessionService.getUserIdBySessionToken(token);
                    var auth = new UsernamePasswordAuthenticationToken(
                            userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (SessionException ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }
}