package sk.tuke.gamestudio.service;

public interface BestResultService {
    void updateBestTime(int userId, int levelId, int timeMs) throws BestResultException;
    void updateBestScore(int userId, int levelId, int timeMs) throws BestResultException;
    Integer getBestTime(int userId, int levelId) throws BestResultException;
    Integer getBestScore(int userId, int levelId) throws BestResultException;
}
