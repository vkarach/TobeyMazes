package sk.tuke.gamestudio.service.impl.JPA;

import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.BestLevelResult;
import sk.tuke.gamestudio.entity.BestLevelResultId;
import sk.tuke.gamestudio.repository.BestLevelResultRepository;
import sk.tuke.gamestudio.service.BestResultService;
import sk.tuke.gamestudio.entity.UserScore;

import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BestResultServiceJPA implements BestResultService {
    private final BestLevelResultRepository bestLevelResultRepository;

    public BestResultServiceJPA(BestLevelResultRepository bestLevelResultRepository) {
        this.bestLevelResultRepository = bestLevelResultRepository;
    }

    public void updateBestTime(int userId, int levelId, long timeMs) {
        BestLevelResultId id = new BestLevelResultId(userId, levelId);
        BestLevelResult result = bestLevelResultRepository.findById(id)
                .orElse(new BestLevelResult(userId, levelId));
        result.setBestTimeMs(timeMs);
        bestLevelResultRepository.save(result);
    }

    public void updateBestScore(int userId, int levelId, int bestScore) {
        BestLevelResultId id = new BestLevelResultId(userId, levelId);
        BestLevelResult result = bestLevelResultRepository.findById(id)
                .orElse(new BestLevelResult(userId, levelId));
        result.setBestScore(bestScore);
        bestLevelResultRepository.save(result);
    }

    public Long getBestTime(int userId, int levelId) {
        BestLevelResultId id = new BestLevelResultId(userId, levelId);
        Optional<BestLevelResult> bestLevelResult =  bestLevelResultRepository.findById(id);
        return bestLevelResult.map(BestLevelResult::getBestTimeMs).orElse(null);
    }

    public Integer getBestScore(int userId, int levelId) {
        BestLevelResultId id = new BestLevelResultId(userId, levelId);
        Optional<BestLevelResult> bestLevelResult = bestLevelResultRepository.findById(id);
        return bestLevelResult.map(BestLevelResult::getBestScore).orElse(null);
    }

    public Integer getBestOverallScore(int userId) {
        Optional<Integer> bestOverallScore =
                bestLevelResultRepository.getBestOverallScoreByUserId(userId);
        return bestOverallScore.orElse(null);
    }

    public List<UserScore> getTopByScore() {
        return bestLevelResultRepository.getTopTenUsers(PageRequest.of(0, 10));
    }

    public List<BestLevelResult> getBestResultsByUserId(int userId) {
        return bestLevelResultRepository.getBestResultsByUserId(userId);
    }
}
