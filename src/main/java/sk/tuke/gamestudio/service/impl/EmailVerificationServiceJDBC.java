package sk.tuke.gamestudio.service.impl;

import sk.tuke.gamestudio.service.EmailVerificationService;
import sk.tuke.gamestudio.service.exception.EmailException;

import java.sql.*;

public class EmailVerificationServiceJDBC implements EmailVerificationService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "as2368";

    public static final String SELECT_EMAIL_CODE_BY_USER_ID =
            "SELECT email_code FROM verification_emails WHERE user_id = ? " +
                    "AND expire_at > CURRENT_TIMESTAMP";

    public static final String SAVE_EMAIL_CODE_BY_USER_ID =
            "INSERT INTO verification_emails (user_id, email_code) VALUES (?, ?)";

    @Override
    public Integer getEmailCodeByUserId(int userId) {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(SELECT_EMAIL_CODE_BY_USER_ID)
        ) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("email_code");
                }
            }
            return null;
        }
        catch (SQLException e) {
            throw new EmailException("Problem getting email code", e);
        }
    }

    @Override
    public void saveEmailVerificationCode(int userId, int code) {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(SAVE_EMAIL_CODE_BY_USER_ID)
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, code);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new EmailException("Problem saving email code", e);
        }
    }
}
