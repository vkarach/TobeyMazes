package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.service.exception.EmailException;

public interface EmailVerificationService {
    Integer getCodeByEmail(String email) throws EmailException;
    void saveEmailVerificationCode(String email, int code) throws EmailException;
    void expireEmail(String email) throws EmailException;
}
