package sk.tuke.gamestudio.game.logicalmazes;

import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.LevelUI;
import sk.tuke.gamestudio.game.logicalmazes.core.*;

public class LogicalMaze {
    public static void main(String[] args) {
        Console console = new Console();

        Game game = new Game(console);

        game.launch();

        game.exit();
    }
}
