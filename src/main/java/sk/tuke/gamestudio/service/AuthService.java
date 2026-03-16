package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.User;

public interface AuthService {
    User register(String name, String password, String email);
    User login(String name, String password);

    void saveSession(String sessionToken);
    User getUserBySessionToken();
    void updateSession(int userId);
    void deleteSession();

    int getOrCreateEmailVerificationCode(int userId);
    int sendOrGetVerificationCodeByEmail(String email);
    void changePassword(int userId, String newPassword);

    void expireEmail(String email);
    void expireEmailByUserId(int userId);
    boolean emailTaken(String email);

    boolean userNameTaken(String userName);
}
