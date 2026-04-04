package sk.tuke.gamestudio.game.logicalmazes.core;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.ConfigurableApplicationContext;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.FxglApp;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan(
        basePackages = "sk.tuke.gamestudio",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "sk\\.tuke\\.gamestudio\\.server\\..*")
)
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
