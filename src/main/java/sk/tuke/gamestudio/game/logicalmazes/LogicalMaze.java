package sk.tuke.gamestudio.game.logicalmazes;

import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.ConsoleUI;
import sk.tuke.gamestudio.game.logicalmazes.core.Field;
import sk.tuke.gamestudio.game.logicalmazes.core.MapParser;
import sk.tuke.gamestudio.game.logicalmazes.core.Player;

public class LogicalMaze {
    public static void main(String[] args) throws Exception {
        Console console = new Console();

        MapParser mapParser = new MapParser("map_1.txt");
        Field mapField = mapParser.getMapField();
        Player player = mapParser.getPlayer();


        ConsoleUI consoleUI = new ConsoleUI(console);

//        consoleUI.drawGame(mapField, player);
        console.clear();
        while (true) {
            Console.Action a = console.readAction();
            if (a != Console.Action.NONE) {
                consoleUI.drawGame(mapField, player, true);
                System.out.println("command:" + a); // debug
            }
            if (a == Console.Action.QUIT) {
                break;
            }
        }
        console.close();
    }
}
