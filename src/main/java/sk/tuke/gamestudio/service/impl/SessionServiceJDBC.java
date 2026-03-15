package sk.tuke.gamestudio.service.impl;

import sk.tuke.gamestudio.service.SessionService;
import sk.tuke.gamestudio.service.exception.UserException;

import java.sql.*;
import java.util.UUID;

public class SessionServiceJDBC implements SessionService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "as2368";

    public static final String INSERT_SESSION =
            "INSERT INTO user_sessions (user_id, session_token) VALUES (?, ?)";

    public static final String SELECT_USER_ID_BY_SESSION_TOKEN =
            "SELECT user_id FROM user_sessions WHERE session_token = ?";

    public static final String UPDATE_SESSION_TOKEN_EXPIRE_DATE =
            "UPDATE user_sessions " +
                    "SET expire_at = CURRENT_TIMESTAMP + INTERVAL '1 month' " +
                    "WHERE session_token = ?";

    public static final String SELECT_USER_SESSION_TOKEN_BY_ID =
            "SELECT session_token FROM user_sessions WHERE user_id = ?";

    public static final String IS_SESSION_TOKEN_EXPIRED =
            "SELECT expire_at < CURRENT_TIMESTAMP FROM user_sessions WHERE session_token = ?";

    @Override
    public String createSession(int userId) {
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

    @Override
    public void updateSessionTokenExpireDate(String sessionToken) {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(UPDATE_SESSION_TOKEN_EXPIRE_DATE)
        ) {
            statement.setString(1, sessionToken);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new UserException("Problem updating session expire date", e);
        }
    }

    @Override
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

    @Override
    public boolean sessionTokenExpired(String sessionToken) {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(IS_SESSION_TOKEN_EXPIRED)
        ) {
            statement.setString(1, sessionToken);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
                return true;
            }
        }
        catch (SQLException e) {
            throw new UserException("Problem checking session expiration", e);
        }
    }
}
