package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;

public class CustomLoadingScene extends LoadingScene {
    private static final int FRAME_W = 70, FRAME_H = 70;
    private static final int BAR_WIDTH = 480;

    private final ImageView animation;
    private final ProgressBar progressBar;
    private final double barStartX;

    public CustomLoadingScene() {
        ImageView bg = new ImageView(new Image(FileReader.getInputStream("ui/shared/backgrounds/main/origbig.png")));
        bg.setSmooth(false);
        bg.setFitWidth(FXGL.getAppWidth());
        bg.setFitHeight(FXGL.getAppHeight());

        Text text1 = FxglUi.createTitle("TOBEY", FxglUi.DEFAULT_TITLE_COLOR);
        double y = FxglUi.DEFAULT_PAD + text1.getLayoutBounds().getHeight();
        text1.setTranslateY(y);
        text1.setTranslateX((FXGL.getAppWidth() - text1.getLayoutBounds().getWidth()) / 2.0 - 50);

        Text text2 = FxglUi.createTitle("MAZES", FxglUi.DEFAULT_TITLE_COLOR);
        text2.setTranslateY(y * 2);
        text2.setTranslateX((FXGL.getAppWidth() - text2.getLayoutBounds().getWidth()) / 2.0 + 50);

        barStartX = (FXGL.getAppWidth() - BAR_WIDTH) / 2.0;
        progressBar = new ProgressBar(0);
        progressBar.setStyle("""
              -fx-accent: rgb(245, 197, 24);                                                                                                                                           \s
              -fx-control-inner-background: rgb(30, 30, 30);                                                                                                                           \s
              -fx-background-color: transparent;                                                                                                                                       \s
              -fx-border-color: rgb(245, 197, 24);
              -fx-border-width: 2;                                                                                                                                                     \s
         """);
        progressBar.setTranslateX(barStartX);
        progressBar.setTranslateY(FXGL.getAppHeight() - 200);
        progressBar.setPrefWidth(BAR_WIDTH);

        animation = createSpriteAnimation("ui/fxgl/animations/KonekTobeySalto70x70.png", 13);
        animation.setTranslateX(barStartX - FRAME_W / 2.0);
        animation.setTranslateY(progressBar.getTranslateY() - FRAME_H - 4);

        getContentRoot().getChildren().addAll(bg, text1, text2, progressBar, animation);
    }

    @Override
    protected void bind(Task<?> task) {
        double[] progress = {0};
        Timeline fakeProgress = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            progress[0] += (0.95 - progress[0]) * 0.05;
            progressBar.setProgress(progress[0]);
            animation.setTranslateX(barStartX + progress[0] * BAR_WIDTH - FRAME_W / 2.0);
        }));
        fakeProgress.setCycleCount(Timeline.INDEFINITE);
        fakeProgress.play();

        task.setOnSucceeded(e -> {
            fakeProgress.stop();
            Timeline[] fillToEnd = {null};
            fillToEnd[0] = new Timeline(new KeyFrame(Duration.millis(30), ev -> {
                progress[0] = Math.min(progress[0] + 0.02, 1.0);
                progressBar.setProgress(progress[0]);
                animation.setTranslateX(barStartX + progress[0] * BAR_WIDTH - FRAME_W / 2.0);
                if (progress[0] >= 1.0) {
                    fillToEnd[0].stop();
                    getController().gotoPlay();
                }
            }));
            fillToEnd[0].setCycleCount(Timeline.INDEFINITE);
            fillToEnd[0].play();
        });
    }

    private ImageView createSpriteAnimation(String path, int totalFrames) {
        int[] frame = {0};
        ImageView view = new ImageView(new Image(FileReader.getInputStream(path)));
        view.setSmooth(false);
        view.setViewport(new Rectangle2D(0, 0, FRAME_W, FRAME_H));

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            frame[0] = (frame[0] + 1) % totalFrames;
            view.setViewport(new Rectangle2D(frame[0] * FRAME_W, 0, FRAME_W, FRAME_H));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        return view;
    }
}