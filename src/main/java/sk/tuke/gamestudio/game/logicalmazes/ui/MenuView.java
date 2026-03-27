package sk.tuke.gamestudio.game.logicalmazes.ui;

import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.service.ReviewService;

public interface MenuView {
    MenuOption mainMenu();
    Level selectLevel(User currentUser);
    void reviewPage(User currentUser, ReviewService reviewService);
    void aboutPage();
    void winPage(long playedTimeMs, int points, boolean isTimeRecord, boolean isScoreRecord);
    void leaderboardPage(User user);
    ProfileOption guestProfilePage();
    ProfileOption authorizedProfilePage(User user);
    void exit();
}
