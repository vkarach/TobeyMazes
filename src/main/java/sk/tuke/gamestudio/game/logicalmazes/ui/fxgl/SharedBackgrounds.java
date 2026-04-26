package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("fxgl")
public class SharedBackgrounds {

    @Bean("mainMenuBackground")
    public ParallaxBackground mainMenuBackground() {
        return new ParallaxBackground(
            new String[]{
                "ui/shared/backgrounds/main/1.png",
                "ui/shared/backgrounds/main/2.png",
                "ui/shared/backgrounds/main/3.png",
                "ui/shared/backgrounds/main/4_with_konek.png"
            },
            new double[]{12, 32, 48, 80}
        );
    }

    @Bean("gameBackground")
    public ParallaxBackground gameBackground() {
        // Web uses the "main" assets but with plain 4.png (no konek) for game.
        return new ParallaxBackground(
            new String[]{
                "ui/shared/backgrounds/main/1.png",
                "ui/shared/backgrounds/main/2.png",
                "ui/shared/backgrounds/main/3.png",
                "ui/shared/backgrounds/main/4.png"
            },
            new double[]{12, 32, 48, 80}
        );
    }

    @Bean("profileBackground")
    public ParallaxBackground profileBackground() {
        return new ParallaxBackground(
            new String[]{
                "ui/shared/backgrounds/profile/1.png",
                "ui/shared/backgrounds/profile/2.png",
                "ui/shared/backgrounds/profile/3_bg_with_konek.png",
                "ui/shared/backgrounds/profile/4.png"
            },
            new double[]{12, 32, 48, 80}
        );
    }

    @Bean("aboutBackground")
    public ParallaxBackground aboutBackground() {
        // Web has two konek variants on layers 3 + 4 — we randomise one per app run.
        boolean konekOnLayer3 = Math.random() < 0.5;
        String layer3 = konekOnLayer3
                ? "ui/shared/backgrounds/about/3_with_konek.png"
                : "ui/shared/backgrounds/about/3.png";
        String layer4 = konekOnLayer3
                ? "ui/shared/backgrounds/about/4_with_hammock.png"
                : "ui/shared/backgrounds/about/4_with_hammock_and_konek.png";
        return new ParallaxBackground(
            new String[]{
                "ui/shared/backgrounds/about/1.png",
                "ui/shared/backgrounds/about/2.png",
                layer3,
                layer4,
                "ui/shared/backgrounds/about/5.png"
            },
            new double[]{12, 32, 48, 64, 80}
        );
    }

    @Bean("leaderboardBackground")
    public ParallaxBackground leaderboardBackground() {
        return new ParallaxBackground(
            new String[]{
                "ui/shared/backgrounds/leaderboard/1.png",
                "ui/shared/backgrounds/leaderboard/2.png",
                "ui/shared/backgrounds/leaderboard/3_with_konek.png",
                "ui/shared/backgrounds/leaderboard/4.png",
                "ui/shared/backgrounds/leaderboard/5.png"
            },
            new double[]{12, 32, 48, 64, 80}
        );
    }
}
