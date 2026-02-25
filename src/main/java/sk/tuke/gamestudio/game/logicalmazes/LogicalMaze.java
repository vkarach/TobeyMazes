package sk.tuke.gamestudio.game.logicalmazes;

import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.LevelUI;
import sk.tuke.gamestudio.game.logicalmazes.core.*;

public class LogicalMaze {
    public static void main(String[] args) throws Exception {
        Console console = new Console();
        LevelUI levelUI = new LevelUI(console);

        Game game = new Game(console, levelUI);
        game.loadLevel("map_1.txt");

        game.launch(); // may be launch and all from there

        game.exit();
    }
}
