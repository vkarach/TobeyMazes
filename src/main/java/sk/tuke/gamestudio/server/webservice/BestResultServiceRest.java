package sk.tuke.gamestudio.server.webservice;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sk.tuke.gamestudio.entity.BestLevelResult;
import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.service.BestResultService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class BestResultServiceRest {
    private final BestResultService bestResultService;

    public BestResultServiceRest(BestResultService bestResultService) {
        this.bestResultService = bestResultService;
    }

    @PutMapping("/{userId}/levels/{levelId}/best-time")
    public void updateBestTime(
            @PathVariable int userId,
            @PathVariable int levelId,
            @RequestParam long timeMs,
            Authentication auth
    ) {
        requireOwnership(auth, userId);
        bestResultService.updateBestTime(userId, levelId, timeMs);
    }

    @PutMapping("/{userId}/levels/{levelId}/best-score")
    public void updateBestScore(
            @PathVariable int userId,
            @PathVariable int levelId,
            @RequestParam int bestScore,
            Authentication auth
    ) {
        requireOwnership(auth, userId);
        bestResultService.updateBestScore(userId, levelId, bestScore);
    }

    private void requireOwnership(Authentication auth, int targetUserId) {
        int tokenUserId = (Integer) auth.getPrincipal();
        if (tokenUserId != targetUserId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{userId}/levels/{levelId}/best-time")
    public Long getBestTime(
            @PathVariable int userId,
            @PathVariable int levelId
    ) {
        System.out.println("controller entered");
        return bestResultService.getBestTime(userId, levelId);
    }

    @GetMapping("/{userId}/levels/{levelId}/best-score")
    public Integer getBestScore(
            @PathVariable int userId,
            @PathVariable int levelId
    ) {
        return bestResultService.getBestScore(userId, levelId);
    }

    @GetMapping("/{userId}/overall-score")
    public Integer getOverallScore(@PathVariable int userId) {
        return bestResultService.getBestOverallScore(userId);
    }

    @GetMapping("/leaderboard")
    public List<UserScore> getTopResults() {
        return bestResultService.getTopByScore();
    }

    @GetMapping("/{userId}/best-results")
    public List<BestLevelResult> getBestResultsByUserId(@PathVariable int userId) {
        return bestResultService.getBestResultsByUserId(userId);
    }
}