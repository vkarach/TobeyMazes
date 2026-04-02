package sk.tuke.gamestudio.game.logicalmazes;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglApp;

import java.util.Arrays;

@SpringBootApplication(scanBasePackages = "sk.tuke.gamestudio")
public class TobeyMazes {
    public static void main(String[] args) {
        boolean fxgl = Arrays.asList(args).contains("--ui=fxgl");
        if (fxgl) {
            FxglApp.launch(args);
        }
        else {
            ConfigurableApplicationContext ctx =
                    new SpringApplicationBuilder(TobeyMazes.class)
                            .web(WebApplicationType.NONE)
                            .run(args);
            ctx.getBean(Game.class).launch();
        }
    }
}
