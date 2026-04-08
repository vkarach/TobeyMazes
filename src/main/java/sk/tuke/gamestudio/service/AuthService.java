package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.User;

public interface AuthService {
    // Auth
    User login(String name, String password);
    void saveSession(String sessionToken);
    User getUserBySessionToken();
    void updateSession(int userId);
    void deleteSession();
    boolean emailTaken(String email);
    boolean userNameTaken(String userName);

    // Registration with server-side email verification
    void initiateEmailVerification(String email);
    User registerWithCode(String name, String password, String email, int code);

    // Password change with server-side email verification
    void initiatePasswordChange(int userId);
    boolean changePasswordWithCode(int userId, int code, String newPassword);

    // Legacy - server-side only (web controllers)
    User register(String name, String password, String email);
    int getOrCreateEmailVerificationCode(int userId);
    int sendOrGetVerificationCodeByEmail(String email);
    void changePassword(int userId, String newPassword);
    void expireEmail(String email);
    void expireEmailByUserId(int userId);
}
