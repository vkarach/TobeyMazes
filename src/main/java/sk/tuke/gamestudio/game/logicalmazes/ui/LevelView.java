package sk.tuke.gamestudio.game.logicalmazes.ui;

import sk.tuke.gamestudio.game.logicalmazes.core.Field;
import sk.tuke.gamestudio.game.logicalmazes.core.Player;

public interface LevelView {
    void launchLevel(Field field);
    void renderField(Field field, Player player);
    void updateHud(long startTime, int targetCount, int points);
    void renderTips();
}
