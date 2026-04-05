package sk.tuke.gamestudio.server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("server")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ApiTokenAuthFilter tokenFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // user mutations
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/users/*/password/change").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/users/*/levels/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/users/*/review").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/users/*/password").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/users/*/email").authenticated()

                        // email codes never exposed externally
                        .requestMatchers("/api/emails/**").authenticated()

                        // session tokens by userId
                        .requestMatchers(HttpMethod.POST, "/api/sessions/*/token").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/sessions/*/token").authenticated()

                        // password change flow
                        .requestMatchers(HttpMethod.POST, "/api/auth/send-password-change-code").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/change-password-with-code").authenticated()

                        .anyRequest().permitAll()
                )
                .build();
    }
}