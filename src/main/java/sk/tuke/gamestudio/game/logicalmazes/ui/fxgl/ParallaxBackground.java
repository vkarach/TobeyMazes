package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;

import java.util.ArrayList;
import java.util.List;

public class ParallaxBackground {
    private final String[] layers;
    private final double[] speeds;

    private final List<ImageView[]> layerPairs = new ArrayList<>();
    private AnimationTimer timer;
    private boolean started = false;
    private long timerLast = 0;

    public ParallaxBackground(String[] layers, double[] speeds) {
        this.layers = layers;
        this.speeds = speeds;
    }

    public List<Node> getAllNodes() {
        List<Node> all = new ArrayList<>();
        for (ImageView[] pair : layerPairs) {
            all.add(pair[0]);
            all.add(pair[1]);
        }
        return all;
    }

    public void start(GameScene scene) {
        if (started) {
            List<Node> inScene = scene.getUINodes();
            for (ImageView[] pair : layerPairs) {
                for (ImageView v : pair) {
                    if (!inScene.contains(v)) {
                        scene.addUINode(v);
                    }
                }
            }
            if (timer != null) timer.start();
            return;
        }
        started = true;

        for (int i = 0; i < layers.length; i++) {
            Image img = new Image(FileReader.getInputStream(layers[i]));
            double scaleY = (double) FXGL.getAppHeight() / img.getHeight();
            double scaledW = img.getWidth() * scaleY;

            ImageView a = makeView(img, scaledW, 0);
            ImageView b = makeView(img, scaledW, scaledW);
            scene.addUINode(a);
            scene.addUINode(b);
            layerPairs.add(new ImageView[]{a, b});
        }

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (timerLast == 0) { timerLast = now; return; }
                double dt = Math.min((now - timerLast) / 1_000_000_000.0, 1.0 / 30.0);
                timerLast = now;
                for (int i = 0; i < layerPairs.size(); i++) {
                    ImageView[] pair = layerPairs.get(i);
                    double w = pair[0].getFitWidth();
                    for (ImageView v : pair) {
                        v.setTranslateX(v.getTranslateX() - speeds[i] * dt);
                        if (v.getTranslateX() + w <= 0)
                            v.setTranslateX(v.getTranslateX() + w * 2);
                    }
                }
            }
        };
        timer.start();
    }

    public void stop() {
        Platform.runLater(() -> { if (timer != null) timer.stop(); });
    }

    private ImageView makeView(Image img, double scaledW, double x) {
        ImageView v = new ImageView(img);
        v.setSmooth(false);
        v.setPreserveRatio(false);
        v.setFitWidth(scaledW);
        v.setFitHeight(FXGL.getAppHeight());
        v.setTranslateX(x);
        v.setTranslateY(0);
        v.setMouseTransparent(true);
        return v;
    }
}
