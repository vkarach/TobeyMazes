package sk.tuke.gamestudio.service;

import java.util.UUID;
import java.sql.*;

public class UserServiceJDBC implements UserService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "as2368";

    public static final String SELECT_USER_ID_BY_USER_NAME = "SELECT user_id FROM users WHERE user_name = ?";
    public static final String SELECT_USER_NAME_BY_USER_ID = "SELECT user_name FROM users WHERE user_id = ?";
    public static final String SELECT_USER_ID_BY_SESSION_TOKEN = "SELECT user_id FROM user_sessions WHERE session_token = ?";
    public static final String SELECT_USER_SESSION_TOKEN_BY_ID = "SELECT session_token FROM user_sessions WHERE user_id = ?";

    public static final String INSERT_USER = "INSERT INTO users (user_name) VALUES (?) RETURNING user_id";
    public static final String INSERT_SESSION = "INSERT INTO user_sessions (user_id, session_token) VALUES (?, ?)";

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
    public int getUserIdByUserName(String userName) {
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
        }
        catch (SQLException e) {
            throw new UserException("Problem finding user" + userName, e);
        }
        throw new UserException("Can not get user id by name" + userName);
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

    public int createUser(String userName) {
        if (userExists(userName)) {
            throw new UserException("User with this name already exist");
        }
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(INSERT_USER)
        ) {
            statement.setString(1, userName);

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

    public String generateSession(int userId) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(INSERT_SESSION)
        ) {
            String token = UUID.randomUUID().toString();

            statement.setInt(1, userId);
            statement.setString(2, token);
            statement.executeUpdate();

            return token;
        }
        catch (SQLException e) {
            throw new UserException("Problem adding user", e);
        }
    }

    @Override
    public int getUserIdBySessionToken(String sessionToken) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(SELECT_USER_ID_BY_SESSION_TOKEN)
        ) {
            statement.setString(1, sessionToken);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        catch (SQLException e) {
            throw new UserException("Problem finding user", e);
        }
        throw new UserException("Can not get user id by session token" + sessionToken);
    }

    public String getSessionTokenByUserId(int userId) {
        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(SELECT_USER_SESSION_TOKEN_BY_ID)
        ) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("session_token");
                }
            }
        }
        catch (SQLException e) {
            throw new UserException("Problem finding user", e);
        }
        return null;
    }
}
