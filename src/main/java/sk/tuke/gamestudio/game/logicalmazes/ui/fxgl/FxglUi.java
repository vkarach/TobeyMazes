package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;

import java.util.ArrayList;
import java.util.List;

public class FxglUi {
    public static final Color DEFAULT_BACKGROUND = Color.rgb(68, 35, 97);

    public static final Color DEFAULT_TEXT_COLOR = Color.WHITE;
    public static final Color DEFAULT_TITLE_COLOR  = Color.rgb(245, 197, 24);
    public static final Color DEFAULT_BUTTON_COLOR = Color.rgb(145, 205, 255);
    public static final Color DEFAULT_ACTIVATION_COLOR = Color.YELLOW;

    public static final int DEFAULT_PAD = 30;

    public static final int TITLE_SIZE = 62;
    public static final int BUTTON_SIZE = 18;
    public static final int DEFAULT_TEXT_SIZE = 18;

    public static Font createFont(int size) {
        return Font.loadFont(
            FileReader.getInputStream("ui/fxgl/fonts/PressStart2P-Regular.ttf"),
            size
        );
    }

    public static Text createText(String text) {
        return createText(text, DEFAULT_TEXT_SIZE, 0, 0, DEFAULT_TEXT_COLOR);
    }

    public static Text createText(String text, Color color) {
        return createText(text, DEFAULT_TEXT_SIZE, 0, 0, color);
    }

    public static Text createText(String text, int size, Color color) {
        return createText(text, size, 0, 0, color);
    }

    public static Text createText(String text, int size, int translateX, int translateY) {
        return createText(text, size, translateX, translateY, Color.WHITE);
    }

    public static Text createTitle(String text, Color color) {
        return createText(text, TITLE_SIZE, 0, 0, color);
    }

    public static Text createText(String text, int size, int translateX, int translateY, Color color) {
        Font font = createFont(size);
        Text textObj = new Text(text);
        textObj.setFont(font);
        textObj.setFill(color);
        textObj.setStroke(Color.BLACK);
        textObj.setStrokeWidth(size > 30 ? 2.5 : 1.5);

        int shadowOffset;
        if (size > 30) shadowOffset = 4;
        else if (size > 18) shadowOffset = 2;
        else if (size > 14) shadowOffset = 1;
        else shadowOffset = 0;
        textObj.setEffect(pixelShadow(shadowOffset));

        textObj.setTranslateX(translateX);
        textObj.setTranslateY(translateY);
        return textObj;
    }

    public static void clearContentNodes(GameScene scene, List<Node> toKeep) {
        List<Node> current = new ArrayList<>(scene.getUINodes());
        for (Node node : current) {
            if (!toKeep.contains(node)) {
                scene.removeUINode(node);
            }
        }
    }

    public static void addTextCenteredX(GameScene scene, Text text) {
        addTextCenteredX(scene, text, 0);
    }

    public static void addTextCenteredX(GameScene scene, Text text, int shift) {
        scene.addUINode(text);
        text.setTranslateX((FXGL.getAppWidth() - text.getLayoutBounds().getWidth()) / 2.0 + shift);
    }

    private static DropShadow pixelShadow(int offset) {
        if (offset == 0) return null;
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        ds.setRadius(0);
        ds.setSpread(1);
        ds.setOffsetX(offset);
        ds.setOffsetY(offset);
        return ds;
    }
}
