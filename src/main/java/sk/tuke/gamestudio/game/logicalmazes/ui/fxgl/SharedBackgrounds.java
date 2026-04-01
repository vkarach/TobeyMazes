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
                "ui/fxgl/backgrounds/profilePageParallax/1.png",
                "ui/fxgl/backgrounds/profilePageParallax/2.png",
                "ui/fxgl/backgrounds/profilePageParallax/3.png",
                "ui/fxgl/backgrounds/profilePageParallax/4.png"
            },
            new double[]{15, 40, 60, 100}
        );
    }

    @Bean("mainMenuBackground")
    public ParallaxBackground mainMenuBackground() {
        return new ParallaxBackground(
            new String[]{
                "ui/fxgl/backgrounds/mainMenuParallax/1.png",
                "ui/fxgl/backgrounds/mainMenuParallax/2.png",
                "ui/fxgl/backgrounds/mainMenuParallax/3.png",
                "ui/fxgl/backgrounds/mainMenuParallax/4_with_konek.png"
            },
            new double[]{15, 40, 60, 100}
        );
    }

    @Bean("aboutBackground")
    public ParallaxBackground aboutBackground() {
        return new ParallaxBackground(
            new String[]{
                "ui/fxgl/backgrounds/aboutPageParallax/1.png",
                "ui/fxgl/backgrounds/aboutPageParallax/2.png",
                "ui/fxgl/backgrounds/aboutPageParallax/3.png",
                "ui/fxgl/backgrounds/aboutPageParallax/4.png"
            },
            new double[]{15, 40, 60, 100}
        );
    }
}
