package sk.tuke.gamestudio.service.exception;

public class BestResultException extends RuntimeException {
    public BestResultException(String message) {
        super(message);
    }

    public BestResultException(String message, Throwable cause) {
        super(message, cause);
    }
}
