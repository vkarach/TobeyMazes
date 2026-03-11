package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.service.exception.BestResultException;

import java.util.List;
import java.util.Map;

public interface BestResultService {
    void updateBestTime(int userId, int levelId, int timeMs) throws BestResultException;
    void updateBestScore(int userId, int levelId, int timeMs) throws BestResultException;
    Integer getBestTime(int userId, int levelId) throws BestResultException;
    Integer getBestScore(int userId, int levelId) throws BestResultException;
    Integer getBestOverallScore(int userId) throws BestResultException;
    List<UserScore> getTopByScore() throws BestResultException;
    public Map<Integer, Integer> getBestTimesByUser(int userId) throws BestResultException;
}