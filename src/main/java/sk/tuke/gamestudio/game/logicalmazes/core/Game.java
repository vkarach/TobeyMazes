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
                case START       -> handleStartLevel();
                case PROFILE     -> handleProfile();
                case LEADERBOARD -> gameMenu.leaderboardPage(currentUser);
                case ABOUT       -> gameMenu.aboutPage();
                case EXIT        -> { exit(); break mainLoop; }
            }
        }
    }

    public GameState startLevel(long startTime) {
        GameController controller = new GameController(gameField, player);
        GameState gameState = GameState.PLAYING;

        console.clear();
        while (gameState == GameState.PLAYING) {
            InputType inputType = console.readAction();

            if (inputType == InputType.QUIT) {
                gameState = GameState.EXITED;
            }
            else if (inputType != InputType.NONE) {
                controller.onInput(Direction.InputToDirection(inputType));
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

        return gameState;
    }

    public void exit() {
        console.clear(); // clear??
        console.print("exiting...\n");
        console.close();
    }

    private void handleStartLevel() {
        while (true) {
            Level level = gameMenu.selectLevel();
            if (level == null) {
                break;
            }
            loadLevel(level.getFilepath());
            long startTime = System.nanoTime();

            GameState gameState = startLevel(startTime);
            if (gameState == GameState.SOLVED) {
                long playedTime = (System.nanoTime() - startTime);
                gameMenu.winPage(playedTime);
            }
        }
    }

    private void handleProfile() {
        if (currentUser == null) {
            GameMenu.ProfileOption selected;
            do {
                selected = gameMenu.profilePage();
                switch (selected) {
                    case REGISTER -> currentUser = authService.register();
                    case LOGIN -> currentUser = authService.login();
                }
            }
            while (selected != GameMenu.ProfileOption.BACK);
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
