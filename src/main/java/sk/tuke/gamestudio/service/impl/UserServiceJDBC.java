package sk.tuke.gamestudio.service.impl;

import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.exception.UserException;

import java.sql.*;

public class UserServiceJDBC implements UserService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "as2368";

    public static final String SELECT_USER_ID_BY_USER_NAME =
            "SELECT user_id FROM users WHERE LOWER(user_name) = LOWER(?)";

    public static final String SELECT_USER_NAME_BY_USER_ID =
            "SELECT user_name FROM users WHERE user_id = ?";

    public static final String SELECT_PASSWORD_BY_USER_ID =
            "SELECT password FROM users WHERE user_id = ?";

    public static final String CHANGE_PASSWORD_BY_USER_ID =
            "UPDATE users SET password = ? WHERE user_id = ?";

    public static final String INSERT_USER =
            "INSERT INTO users (user_name, password) VALUES (?, ?) RETURNING user_id";

    public static final String DELETE_USER = "DELETE FROM users WHERE user_name = ?";

    @Override
    public boolean userExists(String userName) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(SELECT_USER_ID_BY_USER_NAME)
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
    public Integer getUserIdByUserName(String userName) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(SELECT_USER_ID_BY_USER_NAME)
        ) {
            statement.setString(1, userName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
            return null;
        }
        catch (SQLException e) {
            throw new UserException("Problem finding user" + userName, e);
        }
    }

    @Override
    public String getUserNameByUserId(int userId) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(SELECT_USER_NAME_BY_USER_ID)
        ) {
            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_name"); // id
                }
            }
        }
        catch (SQLException e) {
            throw new UserException("Problem finding user with id" + userId, e);
        }
        throw new UserException("Can not get user id by name with id" + userId);
    }

    @Override
    public String getPasswordByUserId(int userId) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(SELECT_PASSWORD_BY_USER_ID)
        ) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
            return null;
        }
        catch (SQLException e) {
            throw new UserException("Problem finding user with id" + userId, e);
        }
    }

    @Override
    public void changePassword(int userId, String newPassword) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(CHANGE_PASSWORD_BY_USER_ID)
        ) {
            statement.setString(1, newPassword);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new UserException("Problem changing password", e);
        }
    }

    @Override
    public int createUser(String userName, String password) {
        if (userExists(userName)) {
            throw new UserException("User with this name already exist");
        }
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(INSERT_USER)
        ) {
            statement.setString(1, userName);
            statement.setString(2, password);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        catch (SQLException e) {
            throw new UserException("Problem adding user", e);
        }

        throw new UserException("Failed to insert user");
    }

    @Override
    public void deleteUserByName(String userName) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(DELETE_USER)
        ) {
            statement.setString(1, userName);

            int deleted = statement.executeUpdate();
            if (deleted == 0) {
                throw new UserException("User " + userName + "not found");
            }
        }
        catch (SQLException e) {
            throw new UserException("Problem deleting user", e);
        }
    }

//    @Override
//    public String createSession(int userId) {
//        try (
//            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
//            PreparedStatement statement = connection.prepareStatement(INSERT_SESSION)
//        ) {
//            String token = UUID.randomUUID().toString();
//
//            statement.setInt(1, userId);
//            statement.setString(2, token);
//            statement.executeUpdate();
//
//            return token;
//        }
//        catch (SQLException e) {
//            throw new UserException("Problem adding user", e);
//        }
//    }

//    @Override
//    public int getUserIdBySessionToken(String sessionToken) {
//        try (
//            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
//            PreparedStatement statement = connection.prepareStatement(SELECT_USER_ID_BY_SESSION_TOKEN)
//        ) {
//            statement.setString(1, sessionToken);
//            try (ResultSet rs = statement.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt("user_id");
//                }
//            }
//        }
//        catch (SQLException e) {
//            throw new UserException("Problem finding user", e);
//        }
//        throw new UserException("Can not get user id by session token" + sessionToken);
//    }
//
//    @Override
//    public String getSessionTokenByUserId(int userId) {
//        try (
//            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
//            PreparedStatement statement = connection.prepareStatement(SELECT_USER_SESSION_TOKEN_BY_ID)
//        ) {
//            statement.setInt(1, userId);
//            try (ResultSet rs = statement.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getString("session_token");
//                }
//            }
//        }
//        catch (SQLException e) {
//            throw new UserException("Problem finding user", e);
//        }
//        return null;
//    }

//    @Override
//    public boolean sessionTokenExpired(String sessionToken) {
//        try (
//            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
//            PreparedStatement statement = connection.prepareStatement(IS_SESSION_TOKEN_EXPIRED)
//        ) {
//            statement.setString(1, sessionToken);
//            try (ResultSet rs = statement.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getBoolean(1);
//                }
//                return true;
//            }
//        }
//        catch (SQLException e) {
//            throw new UserException("Problem checking session expiration", e);
//        }
//    }

//    @Override
//    public void updateSessionTokenExpireDate(String sessionToken) {
//        try (
//            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
//            PreparedStatement statement = connection.prepareStatement(UPDATE_SESSION_TOKEN_EXPIRE_DATE)
//        ) {
//            statement.setString(1, sessionToken);
//            statement.executeUpdate();
//        }
//        catch (SQLException e) {
//            throw new UserException("Problem updating session expire date", e);
//        }
//    }
}
