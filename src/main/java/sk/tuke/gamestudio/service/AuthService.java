package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.User;

public interface AuthService {
    User register(String name, String password);
    User login(String name, String password);
    void saveSession(String sessionToken);
    User getUserBySessionToken();
    void updateSession(int userId);
    void deleteSession();
}
