package sk.tuke.gamestudio.game.logicalmazes.console;

import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.game.logicalmazes.core.InputType;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;

public class GameMenu {
    private final Console console;
    private final TextRenderer textRenderer;

    private final int selectUIX = 30;

    public enum MenuOption {
        START("Start game"),
        ABOUT("About"),
        EXIT("Exit");

        private final String title;

        MenuOption(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public enum LevelOption {
        INTRODUCTION(Level.INTRODUCTION, null),
        IDK_FOR_NOW(Level.IDK_FOR_NOW, null),
        BACK(null, "Back");

        private final Level level;
        private final String title;

        LevelOption(Level level, String title) {
            this.level = level;
            this.title = title;
        }

        public Level getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return title != null ? title : level.toString();
        }
    }

    public GameMenu(Console console) {
        this.console = console;
        this.textRenderer = new TextRenderer(console);
    }

    public MenuOption launch() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/game_name.txt", 0, 0);

        MenuOption[] actions = new MenuOption[]{
                MenuOption.START,
                MenuOption.ABOUT,
                MenuOption.EXIT,
        };

        MenuOption result = select(actions);

        return result == null ? MenuOption.EXIT : result;
    }

    public Level selectLevel() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/select_level.txt", 0, 0);

        LevelOption[] options = LevelOption.values();

        LevelOption selected = select(options);

        if (selected == null || selected == LevelOption.BACK) {
            return null;
        }

        return selected.getLevel();
    }

    public void showAbout() {
        console.clear();

        textRenderer.renderFromFile("uiTexts/about_title.txt", 0, 0);
        textRenderer.renderFromFile("uiTexts/about_text.txt", 0, 10);

        console.print("▶ Back",
                selectUIX, 15,
                AttributedStyle.DEFAULT.background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK)
        );

        while (true) {
            InputType input = console.readAction();
            if (input == InputType.ENTER || input == InputType.QUIT) {
                break;
            }
        }
    }

    private <T> T select(T[] items) {
        int choose = 0;

        int longest = 0;
        for (T item: items) {
            longest = Math.max(longest, item.toString().length());
        }

        selectLoop:
        while (true) {
            InputType input = console.readAction();

            switch (input) {
                case DOWN  -> choose = (choose + 1) % items.length;
                case UP    -> choose = (choose - 1 + items.length) % items.length;
                case ENTER -> { return items[choose]; }
                case QUIT  -> { break selectLoop; }
            }

            int x = selectUIX;
            int y = 20;
            for (int i = 0; i < items.length; i++) {
                String str = String.format("%-" + longest + "s", items[i].toString());
                if (i == choose) {
                    console.print("▶ " + str,
                            x, y + i,
                            AttributedStyle.DEFAULT.inverse()
                    );
                }
                else {
                    console.print("  " + str, x, y + i);
                }
            }
        }
        return null;
    }
}
// >/▶/➤/▸/» settings or A_REVERSE A_BOLD