package sk.tuke.gamestudio.game.logicalmazes.core;

import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.LevelUI;
import sk.tuke.gamestudio.game.logicalmazes.console.GameMenu;

public class Game {
    private final Console console;
    private final LevelUI levelUI;
    private final GameMenu gameMenu;

    private Field gameField;
    private Player player;
    private int targetCount;

    private GameState gameState;

    public Game(Console console, LevelUI levelUI) {
        this.console = console;
        this.levelUI = levelUI;
        this.gameMenu = new GameMenu(console);
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
                while (true) {
                    Level level = gameMenu.selectLevel();
                    if (level == null) {
                        break;
                    }
                    loadLevel(level.getFilepath());
                    startLevel();
                }
            }
            else if (menuOption == GameMenu.MenuOption.ABOUT) {
                gameMenu.showAbout();
            }
            else if (menuOption == GameMenu.MenuOption.EXIT) {
                exit();
                break;
            }
        }
    }

    public void startLevel() {
        GameController controller = new GameController(gameField, player);
        gameState = GameState.PLAYING;

        long startTime = System.nanoTime();

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
            levelUI.renderGameField(gameField, player, true);

            if (targetCount == 0) {
                gameState = GameState.SOLVED;
            }
        }
        controller.shutdown();
        console.clear();
        console.print("game state is " + gameState + '\n');
    }

    public void exit() {
        console.clear(); // clear??
        console.print("exiting...\n");
        console.close();
    }
}
