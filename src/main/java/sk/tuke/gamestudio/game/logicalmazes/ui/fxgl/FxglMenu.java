package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.core.Level;
import sk.tuke.gamestudio.game.logicalmazes.ui.MenuOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.MenuView;
import sk.tuke.gamestudio.game.logicalmazes.ui.ProfileOption;
import sk.tuke.gamestudio.game.logicalmazes.ui.fxgl.pages.*;
import sk.tuke.gamestudio.service.ReviewService;

@Profile("fxgl")
@Component
public class FxglMenu implements MenuView {
    private final MainMenuPage mainMenuPage;
    private final AboutPage aboutPage;
    private final ProfilePage profilePage;
    private final LeaderboardPage leaderboardPage;
    private final ReviewPage reviewPage;

    public FxglMenu(
        MainMenuPage mainMenuPage,
        AboutPage aboutPage,
        ProfilePage profilePage,
        LeaderboardPage leaderboardPage,
        ReviewPage reviewPage
    ) {
        this.mainMenuPage = mainMenuPage;
        this.aboutPage = aboutPage;
        this.profilePage = profilePage;
        this.leaderboardPage = leaderboardPage;
        this.reviewPage = reviewPage;
    }

    @Override
    public MenuOption mainMenu() {
        return mainMenuPage.show();
    }

    @Override
    public Level selectLevel(User currentUser) { return null; }

    @Override
    public void reviewPage(User currentUser) {
        reviewPage.show();
    }

    @Override
    public void aboutPage() {
        aboutPage.show();
    }

    @Override
    public void winPage(long playedTimeMs, int points, boolean isTimeRecord, boolean isScoreRecord) {}

    @Override
    public void leaderboardPage(User user) {
        leaderboardPage.show();
    }

    @Override
    public ProfileOption guestProfilePage() {
        return profilePage.showGuest();
    }

    @Override
    public ProfileOption authorizedProfilePage(User user) {
        return profilePage.showAuthorized(user);
    }

    @Override
    public void exit() {
        Platform.runLater(() -> FXGL.getGameController().exit());
    }
}