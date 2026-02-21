package sk.tuke.gamestudio.game.logicalmazes;

import com.googlecode.lanterna.input.KeyStroke;
import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.ConsoleUI;
import sk.tuke.gamestudio.game.logicalmazes.core.Field;
import sk.tuke.gamestudio.game.logicalmazes.core.MapParser;

public class LogicalMaze {
    public static void main(String[] args) throws Exception {
        Console console = new Console();
        ConsoleUI consoleUI = new ConsoleUI(console);

        MapParser mapParser = new MapParser("map_1.txt");
        Field mapField = mapParser.getMapField();

        consoleUI.drawGame(mapField);
        console.clear();

        console.print("Test");
        console.print("Boo", 10, 10);
        console.print("Boo", 9, 9, AttributedStyle.RED);

        while (true) {
            Console.InputAction a = console.readAction();
            if (a != Console.InputAction.NONE) {
                console.clear();
                consoleUI.drawGame(mapField);
                System.out.println("command:" + a); // debug
            }
            if (a == Console.InputAction.QUIT) {
                break;
            }
        }
        console.close();
    }
}
