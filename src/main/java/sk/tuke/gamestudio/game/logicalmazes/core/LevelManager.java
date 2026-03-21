package sk.tuke.gamestudio.game.logicalmazes.core;


import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.ConsoleRenderer;
import sk.tuke.gamestudio.game.logicalmazes.console.LevelUI;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;
import sk.tuke.gamestudio.service.BestResultService;

@Component
public class LevelManager {
    private final Console console;
    private final LevelUI levelUI;
    private final MapParser mapParser;
    private final BestResultService bestResultService;
    private final ConsoleRenderer consoleRenderer;

    private final SoundUtil pickupSound = new SoundUtil("sounds/pickup.wav");

    private Level currentLevel;
    private Field gameField;
    private Player player;
    private int targetCount;

    public LevelManager(Console console, BestResultService bestResultService, ConsoleRenderer consoleRenderer) {
        this.console = console;
        this.levelUI = new LevelUI(console);
        this.mapParser = new MapParser();
        this.bestResultService = bestResultService;
        this.consoleRenderer = consoleRenderer;
    }

    public record LevelResult(LevelState levelState, int stepCount, long playedTimeNs) {}

    private void cleanup() {
        currentLevel = null;
        gameField = null;
        player = null;
        targetCount = 0;
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
                kTime = 250;
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
        this.targetCount = result.targetCount();
    }

    private LevelResult startLevel() {
        long startTime = System.nanoTime();
        long elapsedNs = 0;
        int x = (console.getWidth() / 2) - (gameField.getRowCount() * 3);
        int y = 20;
        int lowerBoundPad = gameField.getRowCount() * 2;
        int hudPadX = gameField.getRowCount() * 3 + 5;
        int konekTobeyPadY = lowerBoundPad - consoleRenderer.getRenderFromFileSize("uiTexts/konek_tobey.txt").height() + 1;

        console.clear();
        consoleRenderer.renderFromFile("uiTexts/game_title.txt");
        consoleRenderer.renderFromFile(
                "uiTexts/konek_tobey.txt",
                x + hudPadX,
                y + konekTobeyPadY,
                true
        );

        int stepCont = 0;

        GameController controller = new GameController(gameField, player);


        LevelState levelState = LevelState.PLAYING;
        while (levelState == LevelState.PLAYING) {
            elapsedNs = System.nanoTime() - startTime;

            InputType inputType = console.readAction();

            if (inputType == InputType.QUIT) {
                levelState = LevelState.EXITED;
            }
            if (inputType == InputType.RELOAD) {
                loadLevel(currentLevel.getFilepath());
                return startLevel();
            }
            else if (inputType != InputType.NONE) {
                if (controller.onInput(Direction.InputToDirection(inputType))) {
                    stepCont++;
                }
            }

            if (gameField.takeTarget(player)) {
                pickupSound.play();
                if (--targetCount == 0) {
                    levelState = LevelState.SOLVED;
                }
            }

            levelUI.renderHud(
                    startTime,
                    targetCount,
                    computePoints(elapsedNs, stepCont, currentLevel.getDifficulty()),
                    x + hudPadX, y
            );
            levelUI.renderGameField(gameField, player, x, y);
            levelUI.renderTips(x, y + lowerBoundPad + 2);
        }
        controller.shutdown();

        return new LevelResult(levelState, stepCont, elapsedNs);
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
