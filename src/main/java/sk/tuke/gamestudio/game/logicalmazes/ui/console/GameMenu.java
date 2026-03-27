package sk.tuke.gamestudio.game.logicalmazes.ui.console;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.game.logicalmazes.ui.*;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.pages.*;
import sk.tuke.gamestudio.service.ReviewService;

@Component
public class GameMenu implements MenuView {
    private final Console console;
    private final MainMenuPage mainMenuPage;
    private final LevelSelectPage levelSelectPage;
    private final WinPage winPage;
    private final LeaderboardPage leaderboardPage;
    private final ReviewPage reviewPage;
    private final ProfilePage profilePage;
    private final AboutPage aboutPage;

    public GameMenu(
            Console console,
            MainMenuPage mainMenuPage,
            LevelSelectPage levelSelectPage,
            WinPage winPage,
            LeaderboardPage leaderboardPage,
            ReviewPage reviewPage,
            ProfilePage profilePage,
            AboutPage aboutPage) {
        this.console = console;
        this.mainMenuPage = mainMenuPage;
        this.levelSelectPage = levelSelectPage;
        this.winPage = winPage;
        this.leaderboardPage = leaderboardPage;
        this.reviewPage = reviewPage;
        this.profilePage = profilePage;
        this.aboutPage = aboutPage;
    }

    @Override public MenuOption mainMenu()                      { return mainMenuPage.show(); }
    @Override public Level selectLevel(User user)               { return levelSelectPage.show(user); }
    @Override public void winPage(long ms, int pts, boolean tr, boolean sr) { winPage.show(ms, pts, tr, sr); }
    @Override public void leaderboardPage(User user)            { leaderboardPage.show(user); }
    @Override public void reviewPage(User user, ReviewService rs){ reviewPage.show(user, rs); }
    @Override public void aboutPage()                           { aboutPage.show(); }
    @Override public ProfileOption guestProfilePage()           { return profilePage.showGuest(); }
    @Override public ProfileOption authorizedProfilePage(User u){ return profilePage.showAuthorized(u); }

    @Override
    public void exit() {
        console.clear();
        console.setCursorPosition(0, 0);
        console.print("exiting...\n");
        console.close();
    }
}