package sk.tuke.gamestudio.game.logicalmazes.core;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.ui.GameInput;
import sk.tuke.gamestudio.game.logicalmazes.ui.LevelView;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Profile({"console", "fxgl"})
@Component
public class LevelManager {
    private final LevelView levelView;
    private final GameInput gameInput;

    private final MapParser mapParser;
    private final BestResultService bestResultService;

    private final SoundUtil pickupSound = new SoundUtil("sounds/pickup.wav");

    private Level currentLevel;
    private Field gameField;
    private Player player;
    private final AtomicInteger targetCount = new AtomicInteger(0);

    public LevelManager(GameInput gameInput,  LevelView levelView, BestResultService bestResultService) {
        this.gameInput = gameInput;
        this.levelView = levelView;
        this.mapParser = new MapParser();
        this.bestResultService = bestResultService;

    }

    public record LevelResult(LevelState levelState, int stepCount, long playedTimeNs) {}

    private void cleanup() {
        currentLevel = null;
        gameField = null;
        player = null;
        targetCount.set(0);
    }

    public LevelResult playLevel(Level level) {
        currentLevel = level;

        loadLevel(currentLevel.getFilepath());

        LevelResult result = startLevel();

        cleanup();
        return result;
    }

    public int computePoints(long playedTimeNs, int stepCount, Level.Difficulty difficulty) {
        int maxPoints, kTime, kStep;
        long playedTimeMs   =  playedTimeNs / 1_000_000;
        float playedTimeSec =  (float) playedTimeMs / 1000;

        switch (difficulty) { // todo: balance this shit
            case EASY   -> {
                maxPoints = 500;
                kTime = 30;
                kStep = 10;
            }
            case NORMAL -> {
                maxPoints = 1000;
                kTime = 60;
                kStep = 20;
            }
            case MEDIUM -> {
                maxPoints = 2000;
                kTime = 100;
                kStep = 30;
            }
            case HARD   -> {
                maxPoints = 5000;
                kTime = 150;
                kStep = 50;
            }
            default -> {
                return 0;
            }
        }
        int timePenalty =  stepCount * kStep;
        int stepsPenalty = (int) (playedTimeSec * kTime);

        int points = maxPoints - timePenalty - stepsPenalty;

        return Math.max(0, points);
    }

    private void loadLevel(String filepath) {
        MapParser.Result result = mapParser.parseMap(filepath);
        this.gameField = result.mapField();
        this.player = result.player();
        this.targetCount.set(result.targetCount());
    }

    private LevelResult startLevel() {
        long startTime = System.nanoTime();

        AtomicInteger stepCont = new AtomicInteger(0);
        GameController controller = new GameController(gameField, player);
        LevelState levelState = LevelState.PLAYING;

        levelView.launchLevel(gameField);
        levelView.renderTips();
        // render every 100 ms regardless of input
        ScheduledExecutorService renderScheduler = Executors.newSingleThreadScheduledExecutor();
        renderScheduler.scheduleAtFixedRate(() -> {
            long elapsed = System.nanoTime() - startTime;
            levelView.updateHud(startTime, targetCount.get(), computePoints(elapsed, stepCont.get(), currentLevel.getDifficulty()));
            levelView.renderField(gameField, player);
        }, 0, 75, TimeUnit.MILLISECONDS);

        while (levelState == LevelState.PLAYING) {
            InputType inputType = gameInput.getInput();

            switch (inputType) {
                case QUIT -> levelState = LevelState.EXITED;
                case RELOAD -> {
                    stopScheduler(renderScheduler);
                    controller.shutdown();
                    loadLevel(currentLevel.getFilepath());
                    return startLevel();
                }
                case UP, DOWN, LEFT, RIGHT -> {
                    if (controller.onInput(Direction.InputToDirection(inputType))) {
                        stepCont.incrementAndGet();
                    }
                }
                case NONE -> {}
            }

            if (gameField.takeTarget(player)) {
                pickupSound.play();
                if (targetCount.decrementAndGet() == 0) {
                    levelState = LevelState.SOLVED;
                }
            }
        }

        stopScheduler(renderScheduler);
        controller.shutdown();

        return new LevelResult(levelState, stepCont.get(), System.nanoTime() - startTime);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void stopScheduler(ScheduledExecutorService scheduler) {
        scheduler.shutdownNow();
        try {
            scheduler.awaitTermination(200, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean checkAndUpdateBestTime(int userId, int levelId, int playedTimeMs) {
        Long bestTimeMs = bestResultService.getBestTime(userId, levelId);

        if (bestTimeMs == null || bestTimeMs > playedTimeMs) {
            bestResultService.updateBestTime(userId, levelId, playedTimeMs);
            return true;
        }
        return false;
    }

    public boolean checkAndUpdateBestScore(int userId, int levelId, int score) {
        Integer bestScore = bestResultService.getBestScore(userId, levelId);

        if (bestScore == null || score > bestScore) {
            bestResultService.updateBestScore(userId, levelId, score);
            return true;
        }
        return false;
    }
}
