package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.exception.ScoreException;

import java.util.List;

public interface ScoreService {
    void addScore(int userId, Score score) throws ScoreException;
    List<Score> getTopScores(String game) throws ScoreException;
    void reset() throws ScoreException;
}
