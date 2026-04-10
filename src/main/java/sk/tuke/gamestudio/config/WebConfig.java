package sk.tuke.gamestudio.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sk.tuke.gamestudio.server.security.RememberMeInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private RememberMeInterceptor rememberMeInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/ui/**")
                .addResourceLocations("classpath:/ui/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (rememberMeInterceptor != null) {
            registry.addInterceptor(rememberMeInterceptor)
                    .addPathPatterns("/menu", "/profile", "/profile/**",
                            "/game/**", "/leaderboard", "/review", "/about");
        }
    }
}
