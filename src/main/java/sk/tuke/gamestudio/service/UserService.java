package sk.tuke.gamestudio.service;

public interface UserService {
    boolean userExists(String userName) throws UserException;
    int getUserIdByUserName(String userName) throws UserException;
    String getUserNameByUserId(int useId) throws UserException;
    int createUser(String userName) throws UserException;
    String generateSession(int userId) throws UserException;
    int getUserIdBySessionToken(String sessionToken) throws UserException;
}
