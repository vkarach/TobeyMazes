package sk.tuke.gamestudio.service;

public interface UserService {
    public boolean isUserExist(String userName) throws UserException;
    public int addUser(String userName) throws UserException;
}
