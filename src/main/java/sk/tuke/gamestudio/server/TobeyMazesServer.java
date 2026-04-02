package sk.tuke.gamestudio.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "sk.tuke.gamestudio.server",
        "sk.tuke.gamestudio.service",
        "sk.tuke.gamestudio.config"
})
@EntityScan("sk.tuke.gamestudio.entity")
@EnableJpaRepositories("sk.tuke.gamestudio.repository")
@Configuration
public class TobeyMazesServer {
    public static void main(String[] args) {
        SpringApplication.run(TobeyMazesServer.class, args);
    }
}
