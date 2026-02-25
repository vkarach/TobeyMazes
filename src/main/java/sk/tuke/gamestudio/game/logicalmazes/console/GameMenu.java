package sk.tuke.gamestudio.game.logicalmazes.console;

import java.awt.*;

public class GameMenu {
    private final Console console;

    public enum MenuAction {

        START("Start game"),
        ABOUT("About"),
        EXIT("Exit");

        private final String title;

        MenuAction(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public GameMenu(Console console) {
        this.console = console;
    }

    public MenuAction start() {
        console.clear();
        int choose = 0;

        MenuAction[] menuActions = new MenuAction[]{
                MenuAction.START,
                MenuAction.ABOUT,
                MenuAction.EXIT,
        };

        while (true) {
            Console.InputAction inputAction = console.readAction();
            console.moveCursorToStart();

            if (inputAction == Console.InputAction.DOWN) {
                choose++;
                if (choose > menuActions.length - 1) {
                    choose = 0;
                }
            }
            else if (inputAction == Console.InputAction.UP) {
                choose--;
                if (choose < 0) {
                    choose = menuActions.length - 1;
                }
            }
            else if (inputAction == Console.InputAction.QUIT) {
                break;
            }
            else if (inputAction == Console.InputAction.ENTER) {
                return menuActions[choose];
            }

            for (int i = 0; i < menuActions.length; i++) {
                if (i == choose) {
                    console.print("> " + menuActions[i].getTitle() + '\n');
                }
                else {
                    console.print("  " + menuActions[i].getTitle() + '\n');
                }
            }
        }
        return MenuAction.START; // plug
    }
}
