package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.BestLevelResult;
import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.service.exception.BestResultException;

import java.util.List;
import java.util.Map;

public interface BestResultService {
    void updateBestTime(int userId, int levelId, long timeMs) throws BestResultException;
    void updateBestScore(int userId, int levelId, int score) throws BestResultException;
    Long getBestTime(int userId, int levelId) throws BestResultException;
    Integer getBestScore(int userId, int levelId) throws BestResultException;
    Integer getBestOverallScore(int userId) throws BestResultException;
    default Integer getUserLeaderboardPosition(int userId) throws BestResultException { return null; }
    List<UserScore> getTopByScore() throws BestResultException;
    List<BestLevelResult> getBestResultsByUserId(int userId) throws BestResultException;
}