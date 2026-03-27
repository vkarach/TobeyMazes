package sk.tuke.gamestudio.game.logicalmazes.ui;

import sk.tuke.gamestudio.entity.User;

public interface AuthView {
    User register();
    User login();
    void changePassword(int userId);
}
