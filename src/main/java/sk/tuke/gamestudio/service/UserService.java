package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.service.exception.UserException;

public interface UserService {
    boolean userExists(String userName) throws UserException;
    boolean emailExists(String email) throws UserException;

    int createUser(String userName, String password, String email) throws UserException;
    void deleteUserByName(String userName) throws UserException;

    Integer getUserIdByName(String userName) throws UserException;
    String getUserNameById(int userId) throws UserException;
    String getPasswordByUserId(int userId) throws UserException;
    String getEmailByUserId(int userId) throws UserException;

    void changePassword(int userId, String newPassword) throws UserException;

}
