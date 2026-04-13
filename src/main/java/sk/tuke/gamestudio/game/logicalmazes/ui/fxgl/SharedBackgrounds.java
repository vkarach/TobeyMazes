package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("fxgl")
public class SharedBackgrounds {

    @Bean("profileBackground")
    public ParallaxBackground profileBackground() {
        return new ParallaxBackground(
            new String[]{
                "ui/shared/backgrounds/profile/1.png",
                "ui/shared/backgrounds/profile/2.png",
                "ui/shared/backgrounds/profile/3.png",
                "ui/shared/backgrounds/profile/4.png"
            },
            new double[]{15, 40, 60, 100}
        );
    }

    @Bean("mainMenuBackground")
    public ParallaxBackground mainMenuBackground() {
        return new ParallaxBackground(
            new String[]{
                "ui/shared/backgrounds/main/1.png",
                "ui/shared/backgrounds/main/2.png",
                "ui/shared/backgrounds/main/3.png",
                "ui/shared/backgrounds/main/4_with_konek.png"
            },
            new double[]{15, 40, 60, 100}
        );
    }

    @Bean("aboutBackground")
    public ParallaxBackground aboutBackground() {
        return new ParallaxBackground(
            new String[]{
                "ui/shared/backgrounds/about/1.png",
                "ui/shared/backgrounds/about/2.png",
                "ui/shared/backgrounds/about/3.png",
                "ui/shared/backgrounds/about/4.png"
            },
            new double[]{15, 40, 60, 100}
        );
    }
}
