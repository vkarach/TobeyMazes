package sk.tuke.gamestudio.service;


import sk.tuke.gamestudio.service.exception.SessionException;

public interface SessionService {
    String createSession(int userId) throws SessionException;
    int getUserIdBySessionToken(String sessionToken) throws SessionException;
    void updateSessionTokenExpireDate(String sessionToken) throws SessionException;
    String getSessionTokenByUserId(int userId) throws SessionException;
    boolean sessionTokenExpired(String sessionToken) throws SessionException;
}
