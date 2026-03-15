package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.service.exception.UserException;

public interface UserService {
    boolean userExists(String userName) throws UserException;

    int createUser(String userName, String password) throws UserException;
    void deleteUserByName(String userName) throws UserException;

    Integer getUserIdByUserName(String userName) throws UserException;
    String getUserNameByUserId(int useId) throws UserException;
    String getPasswordByUserId(int userId) throws UserException;

    void changePassword(int userId, String newPassword) throws UserException;

}
