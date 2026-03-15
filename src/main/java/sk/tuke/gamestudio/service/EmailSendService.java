package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.service.exception.EmailException;

public interface EmailSendService {
    void sendCode(String toEmail, int code) throws EmailException;
}
