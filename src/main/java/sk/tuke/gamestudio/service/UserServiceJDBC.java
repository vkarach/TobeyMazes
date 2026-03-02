package sk.tuke.gamestudio.service;

import java.sql.*;

public class UserServiceJDBC implements UserService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "as2368";
    public static final String SELECT = "SELECT user_id FROM users WHERE user_name = ?";
    public static final String INSERT = "INSERT INTO users (user_name) VALUES (?)";
//    public static final String DELETE = "DELETE FROM users";

    @Override
    public boolean isUserExist(String userName) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(SELECT)
        ) {
            statement.setString(1, userName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
        catch (SQLException e) {
            throw new UserException("Problem finding user", e);
        }
    }

    @Override
    public int addUser(String userName) {
        if (isUserExist(userName)) {
            throw new UserException("User with this name already exist");
        }
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(INSERT)
        ) {
            statement.setString(1, userName);
            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // id
            }
        }
        catch (SQLException e) {
            throw new UserException("Problem adding user", e);
        }
        return -1;
    }

    // todo: get user
}
