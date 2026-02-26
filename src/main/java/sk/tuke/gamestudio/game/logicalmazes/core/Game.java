package sk.tuke.gamestudio.game.logicalmazes.core;

import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.LevelUI;
import sk.tuke.gamestudio.game.logicalmazes.console.GameMenu;

public class Game {
    private final MapParser mapParser = new MapParser();
    private final Console console;
    private final LevelUI levelUI;

    private Field gameField;
    private Player player;
    private int targetCount;

    private GameState gameState;

    public Game(Console console, LevelUI levelUI) {
        this.console = console;
        this.levelUI = levelUI;
    }

    public void loadLevel(String filename) {
        MapParser.Result result = mapParser.parseMap(filename);
        this.gameField = result.mapField;
        this.player = result.player;
        this.targetCount = result.targetCount;
    }

    public void launch() {
        GameMenu gameMenu = new GameMenu(console);
        GameMenu.MenuAction menuAction = gameMenu.start();
        if (menuAction == GameMenu.MenuAction.START) {
            startLevel(); // todo: how to choose level???
        }
        else if (menuAction == GameMenu.MenuAction.EXIT) {
            exit();
        }
    }

    public void startLevel() {
        GameController controller = new GameController(gameField, player); // todo: where?
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
