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
        while (true) {
            GameMenu.MenuOption menuOption = gameMenu.launch();
            if (menuOption == GameMenu.MenuOption.START) {
                handleStartLevel();
            }
            else if (menuOption == GameMenu.MenuOption.PROFILE) {
                handleProfile();
            }
            else if (menuOption == GameMenu.MenuOption.ABOUT) {
                gameMenu.aboutPage();
            }
            else if (menuOption == GameMenu.MenuOption.EXIT) {
                exit();
                break;
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
            levelUI.renderHud(startTime, targetCount, gameField.getRowCount() * 3 + 5, 0);
            levelUI.renderGameField(gameField, player);

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
            String selected = gameMenu.profilePage();
            if (selected.equals("Login")) { // fuck you ide :)
                currentUser = authService.startLogin();
            }
        }
        if (currentUser != null) {
            gameMenu.profilePage(currentUser);
        }
    }

}
