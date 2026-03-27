package sk.tuke.gamestudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;

@SpringBootApplication
public class TobeyMazes {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TobeyMazes.class, args);

        Game game = context.getBean(Game.class);
        game.launch();
    }
}
