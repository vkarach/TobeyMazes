package sk.tuke.gamestudio.service.impl.JDBC;

import sk.tuke.gamestudio.service.EmailVerificationService;
import sk.tuke.gamestudio.service.exception.EmailException;

import java.sql.*;

public class EmailVerificationServiceJDBC implements EmailVerificationService {
    public static final String SELECT_EMAIL_CODE_BY_USER_ID =
            "SELECT email_code FROM verification_emails WHERE email = ? " +
                    "AND expire_at > CURRENT_TIMESTAMP";

    public static final String INSERT_EMAIL_CODE_AND_EMAIL =
            "INSERT INTO verification_emails (email, email_code) VALUES (?, ?)";

    public static final String EXPIRE_EMAIL_BY_USER_ID =
            "UPDATE verification_emails SET expire_at = CURRENT_TIMESTAMP WHERE email = ?";


    @Override
    public Integer getCodeByEmail(String email) {
        try (
            Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(SELECT_EMAIL_CODE_BY_USER_ID)
        ) {
            statement.setString(1, email);
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
    public void saveEmailVerificationCode(String email, int code) {
        try (
                Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_EMAIL_CODE_AND_EMAIL)
        ) {
            statement.setString(1, email);
            statement.setInt(2, code);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new EmailException("Problem saving email code", e);
        }
    }

    @Override
    public void expireEmail(String email) {
        try (
            Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(EXPIRE_EMAIL_BY_USER_ID)
        ) {
            statement.setString(1, email);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new EmailException("Problem saving email code", e);
        }
    }
}
