package sk.tuke.gamestudio.game.logicalmazes;


import sk.tuke.gamestudio.game.logicalmazes.consoleui.ConsoleUI;
import sk.tuke.gamestudio.game.logicalmazes.core.Field;

import static sk.tuke.gamestudio.game.logicalmazes.core.MapParser.parseMap;

public class LogicalMaze {
    public static void main(String[] args) {
        Field mapField = parseMap("map_1.txt");
        ConsoleUI consoleUI = new ConsoleUI(mapField);

        for (int i = 0; i < 1000; i++) {
            consoleUI.drawField();
        }
    }
}
