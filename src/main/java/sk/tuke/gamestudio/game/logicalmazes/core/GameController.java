package sk.tuke.gamestudio.game.logicalmazes.core;

import java.util.concurrent.*;

public class GameController {
    private final Field mapField;
    private final Player player;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> movementTask;

    private volatile boolean moving = false;
    private volatile Direction pending = null;
    private volatile Direction activeDir = null;

    private static final long PENDING_SAFE_BOUND_NS = 300_000_000L;
     private volatile long pendingSavedNs = 0;

    public GameController(Field mapField, Player player) {
        this.mapField = mapField;
        this.player = player;
    }

    public void onInput(Direction dir) {
        if (moving) {
            pending = dir;
            pendingSavedNs = System.nanoTime();
            return;
        }
        startMove(dir);
    }

    private void startMove(Direction dir) {
        moving = true;
        activeDir = dir;
        pending = null;

        movementTask = scheduler.scheduleAtFixedRate(() -> {
            if (!mapField.canStep(player, activeDir)) {
                stopMove();
                return;
            }

            mapField.step(player, activeDir);
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void stopMove() {
        ScheduledFuture<?> task = movementTask;
        if (task != null) task.cancel(false);

        moving = false;
        activeDir = null;
        long pendingAgeNs = System.nanoTime() - pendingSavedNs;

        Direction next = pending;
        pending = null;
        pendingSavedNs = 0;

        if (next != null && pendingAgeNs < PENDING_SAFE_BOUND_NS) {
            startMove(next);
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}