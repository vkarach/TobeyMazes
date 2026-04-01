package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.ui.GameInput;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Profile("fxgl")
@Component
public class FxglInput implements GameInput {
    private final BlockingQueue<InputType> queue = new ArrayBlockingQueue<>(1);

    public void push(InputType input) {
        queue.offer(input);
    }

    @Override
    public InputType getInput() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return InputType.QUIT;
        }
    }
}