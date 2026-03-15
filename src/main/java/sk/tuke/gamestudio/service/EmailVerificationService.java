package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.service.exception.EmailException;

public interface EmailVerificationService {
    Integer getEmailCodeByUserId(int userId) throws EmailException;
    void saveEmailVerificationCode(int userId, int code) throws EmailException;
}
