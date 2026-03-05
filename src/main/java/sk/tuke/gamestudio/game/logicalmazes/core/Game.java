package sk.tuke.gamestudio.game.logicalmazes.core;

import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.LevelUI;
import sk.tuke.gamestudio.game.logicalmazes.console.GameMenu;

public class Game {
    private final Console console;
    private final LevelUI levelUI;
    private final GameMenu gameMenu;
    private final AuthService authService;

    private User currentUser;

    private Field gameField;
    private Player player;
    private int targetCount;

    public Game(Console console, LevelUI levelUI) {
        this.console = console;
//        console.waitForScale(w, h); // todo: maybe this
        this.levelUI = levelUI;
        this.gameMenu = new GameMenu(console);
        this.authService = new AuthService(console);
        this.currentUser = authService.loadUserSession();
    }

    public void loadLevel(String filepath) {
        MapParser.Result result = new MapParser().parseMap(filepath);
        this.gameField = result.mapField;
        this.player = result.player;
        this.targetCount = result.targetCount;
    }

    public void launch() {
        mainLoop:
        while (true) {
            GameMenu.MenuOption menuOption = gameMenu.launch();
            switch (menuOption) {
                case START       -> handleStartAndPlayLevel();
                case PROFILE     -> handleProfile();
                case LEADERBOARD -> gameMenu.leaderboardPage(currentUser);
                case ABOUT       -> gameMenu.aboutPage();
                case EXIT        -> { exit(); break mainLoop; }
            }
        }
    }

    private int computePoints(Level.Difficulty difficulty, int stepsCount, long playedTimeMs) {
        int maxPoints;
        int kTime;
        int kStep;
        float playedTimeSec = (float) playedTimeMs / 1000;

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
                return -1;
            }
        }
        int timePenalty =  stepsCount * kStep;
        int stepsPenalty = (int) (playedTimeSec * kTime);

        int points = maxPoints - timePenalty - stepsPenalty;

        return Math.max(0, points);
    }

    public PlayedResult startLevel(long startTime) {
        GameController controller = new GameController(gameField, player);
        GameState gameState = GameState.PLAYING;
        int stepCont = 0;
        console.clear();
        while (gameState == GameState.PLAYING) {
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
                targetCount--;
            }
            int x = 0;
            int y = 0;
            levelUI.renderHud(startTime, targetCount, x + gameField.getRowCount() * 3 + 5, y);
            levelUI.renderGameField(gameField, player, x, y);

            if (targetCount == 0) {
                gameState = GameState.SOLVED;
            }
        }
        controller.shutdown();

        return new PlayedResult(gameState, stepCont, System.nanoTime() - startTime);
    }

    public static class PlayedResult {
        public GameState gameState;
        public int stepCount;
        public long playedTimeNs;

        public PlayedResult(GameState gameState, int stepCount, long playedTimeNs) {
            this.gameState = gameState;
            this.stepCount = stepCount;
            this.playedTimeNs = playedTimeNs;
        }
    }

    public void exit() {
        console.clear(); // clear??
        console.print("exiting...\n");
        console.close();
    }

    private void handleStartAndPlayLevel() {
        while (true) {
            Level level = gameMenu.selectLevel();
            if (level == null) {
                break;
            }
            loadLevel(level.getFilepath());
            long startTime = System.nanoTime();

            PlayedResult playedResult = startLevel(startTime);
            if (playedResult.gameState == GameState.SOLVED) {
                int points = computePoints(
                        level.getDifficulty(),
                        playedResult.stepCount,
                        playedResult.playedTimeNs / 1_000_000
                );
                gameMenu.winPage(playedResult.playedTimeNs, points);
            }
        }
    }

    private void handleProfile() {
        if (currentUser == null) {
            GameMenu.ProfileOption selected;
            do {
                selected = gameMenu.profilePage();
                if (selected == null) return;
                switch (selected) {
                    case REGISTER -> currentUser = authService.register();
                    case LOGIN -> currentUser = authService.login();
                }
            }
            while (currentUser == null && selected != GameMenu.ProfileOption.BACK);
        }
        if (currentUser != null) {
            GameMenu.ProfileOption choose = gameMenu.profilePage(currentUser);
            if (choose == GameMenu.ProfileOption.LOGOUT) {
                authService.deleteSession();
                currentUser = null;
            }
        }
    }
}
