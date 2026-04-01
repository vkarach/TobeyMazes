package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.Field;
import sk.tuke.gamestudio.game.logicalmazes.core.Player;
import sk.tuke.gamestudio.game.logicalmazes.ui.LevelView;

@Profile("fxgl")
@Component
public class FxglLevelView implements LevelView {
    @Override public void launchLevel(Field field) {}
    @Override public void renderField(Field field, Player player) {}
    @Override public void updateHud(long startTime, int targetCount, int points) {}
    @Override public void renderTips() {}
}