package sk.tuke.gamestudio.game.logicalmazes;

import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.LevelUI;
import sk.tuke.gamestudio.game.logicalmazes.core.*;

public class LogicalMaze {
    public static void main(String[] args) {
        Console console = new Console();
        LevelUI levelUI = new LevelUI(console);

        Game game = new Game(console, levelUI);
//        game.loadLevel("maps/map_2.txt");

        game.launch();

        game.exit();
    }
}
