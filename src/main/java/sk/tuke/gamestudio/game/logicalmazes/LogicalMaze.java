package sk.tuke.gamestudio.game.logicalmazes;

import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.ConsoleUI;
import sk.tuke.gamestudio.game.logicalmazes.core.*;

public class LogicalMaze {
    public static void main(String[] args) throws Exception {
        Console console = new Console();
        ConsoleUI consoleUI = new ConsoleUI(console);

        MapParser mapParser = new MapParser("map_1.txt");
        Field gameField = mapParser.getMapField();
        Player player = mapParser.getPlayer();
        int targetCount = mapParser.getTargetCount();

        console.clear();
        consoleUI.renderGameField(gameField, player);

        GameController controller = new GameController(gameField, player);

        long startTime = System.nanoTime();

        while (true) {
            Console.InputAction inputAction = console.readAction();
            if (inputAction == Console.InputAction.QUIT) {
                break;
            }
            else if (inputAction != Console.InputAction.NONE) {
                controller.onInput(Direction.InputToDirection(inputAction));
            }
            if (gameField.takeTarget(player)) {
                if (--targetCount == 0) {
                    break;
                }
            }
            consoleUI.renderHud(startTime, targetCount, 25, 0);

            consoleUI.renderGameField(gameField, player, true);

        }
        System.out.println("exiting...");
        console.close();
        controller.shutdown();
    }
}
