package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import sk.tuke.gamestudio.game.logicalmazes.core.TobeyMazes;
import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;


import static com.almasb.fxgl.dsl.FXGL.onKeyDown;

public class FxglApp extends GameApplication {
    private static String[] appArgs;
    private ConfigurableApplicationContext springContext;

    public static void launch(String[] args) {
        appArgs = args;
        GameApplication.launch(FxglApp.class, args);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("TobeyMazes");
        settings.setVersion(Game.version);
        settings.setManualResizeEnabled(true);
        settings.setFullScreenAllowed(true);
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setGameMenuEnabled(false);
        settings.setSceneFactory(new SceneFactory() {
            @Override @NotNull
            public LoadingScene newLoadingScene() {
                return new CustomLoadingScene();
            }
        });

    }

    @Override
    protected void initGame() {
        springContext = new SpringApplicationBuilder(TobeyMazes.class)
                .properties("spring.profiles.active=fxgl")
                .web(WebApplicationType.NONE)
                .run(appArgs);

        Game game = springContext.getBean(Game.class);
        new Thread(game::launch, "game-thread").start();
    }

    @Override
    protected void initInput() {
        onKeyDown(KeyCode.F11, () -> {
            Stage stage = FXGL.getPrimaryStage();
            stage.setFullScreen(!stage.isFullScreen());
        });

        Image cursor = new Image(FileReader.getInputStream("ui/fxgl/cursors/Arrow.png"), 8, 8, true, true);
        FXGL.getGameScene().setCursor(cursor, new Point2D(0, 0));
    }
}
