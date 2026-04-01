package sk.tuke.gamestudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglApp;

import java.util.Arrays;

@SpringBootApplication
public class TobeyMazes {
    public static void main(String[] args) {
        boolean fxgl = Arrays.asList(args).contains("--ui=fxgl");
        if (fxgl) {
            FxglApp.launch(args);
        }
        else {
            ConfigurableApplicationContext ctx = SpringApplication.run(TobeyMazes.class, args);
            ctx.getBean(Game.class).launch();
        }
    }
}
