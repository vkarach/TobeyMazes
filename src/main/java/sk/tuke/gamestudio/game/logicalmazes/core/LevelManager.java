package sk.tuke.gamestudio.game.logicalmazes.core;


import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.LevelUI;
import sk.tuke.gamestudio.service.impl.BestResultServiceJDBC;
import sk.tuke.gamestudio.service.BestResultService;

public class LevelManager {
    private final Console console;
    private final LevelUI levelUI;
    private final MapParser mapParser;
    private final BestResultService bestResultService;

    private Level currentLevel;
    private Field gameField;
    private Player player;
    private int targetCount;

    public LevelManager(Console console) {
        this.console = console;
        this.levelUI = new LevelUI(console);
        this.mapParser = new MapParser();
        this.bestResultService = new BestResultServiceJDBC();
    }

    public static class LevelResult {
        public GameState gameState;
        public int stepCount;
        public long playedTimeNs;

        public LevelResult(GameState gameState, int stepCount, long playedTimeNs) {
            this.gameState = gameState;
            this.stepCount = stepCount;
            this.playedTimeNs = playedTimeNs;
        }
    }

    private void cleanup() {
        currentLevel = null;
        gameField = null;
        player = null;
        targetCount = 0;
    }

    public LevelResult playLevel(Level level) {
        currentLevel = level;

        loadLevel(currentLevel.getFilepath());

//        new ConsoleRenderer(console);

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
            case MEDIUM -> {
                maxPoints = 2000;
                kTime = 10;
                kStep = 25;
            }
            case HARD   -> {
                maxPoints = 5000;
                kTime = 30;
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
        this.gameField = result.mapField;
        this.player = result.player;
        this.targetCount = result.targetCount;
    }

    private LevelResult startLevel() {
        long startTime = System.nanoTime();
        long elapsedNs = 0;
        int x = (console.getWidth() / 2) - (gameField.getRowCount() * 3);
        int y = 15;

        int stepCont = 0;

        GameController controller = new GameController(gameField, player);

        console.clear();

        GameState gameState = GameState.PLAYING;
        while (gameState == GameState.PLAYING) {
            elapsedNs = System.nanoTime() - startTime;

            InputType inputType = console.readAction();

            if (inputType == InputType.QUIT) {
                gameState = GameState.EXITED;
            }
            else if (inputType != InputType.NONE) {
                if (controller.onInput(Direction.InputToDirection(inputType))) {
                    stepCont++;
                }
            }

            if (gameField.takeTarget(player)) {
                if (--targetCount == 0) {
                    gameState = GameState.SOLVED;
                }
            }

            String str  = String.format(
                    "Points: %03d, steps: %d",
                    computePoints(elapsedNs, stepCont, currentLevel.getDifficulty()),
                    stepCont
            );
            console.print(str, x, y - 1);
            levelUI.renderHud(startTime, targetCount, x + gameField.getRowCount() * 3 + 5, y);
            levelUI.renderGameField(gameField, player, x, y);
        }
        controller.shutdown();

        return new LevelResult(gameState, stepCont, elapsedNs);
    }

    public boolean checkAndUpdateBestTime(int userId, int levelId, int playedTimeMs) {
        Integer bestTimeMs = bestResultService.getBestTime(userId, levelId);

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
