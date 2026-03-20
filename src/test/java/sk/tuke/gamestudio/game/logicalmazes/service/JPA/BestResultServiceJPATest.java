package sk.tuke.gamestudio.game.logicalmazes.service.JPA;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.service.BestResultService;
import sk.tuke.gamestudio.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BestResultServiceJPATest extends BaseJPATest {

    @Autowired
    private BestResultService bestResultService;

    @Autowired
    private UserService userService;

    private int createUser() {
        return userService.createUser(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID() + "@test.com"
        );
    }

    @Test
    public void updateAndGetBestTimeTest() {
        int userId = createUser();

        bestResultService.updateBestTime(userId, 1, 5000L);
        assertEquals(5000L, bestResultService.getBestTime(userId, 1));

        bestResultService.updateBestTime(userId, 1, 3000L);
        assertEquals(3000L, bestResultService.getBestTime(userId, 1));
    }

    @Test
    public void updateAndGetBestScoreTest() {
        int userId = createUser();

        bestResultService.updateBestScore(userId, 1, 100);
        assertEquals(100, bestResultService.getBestScore(userId, 1));

        bestResultService.updateBestScore(userId, 1, 200);
        assertEquals(200, bestResultService.getBestScore(userId, 1));
    }

    @Test
    public void getBestOverallScoreTest() {
        int userId = createUser();

        bestResultService.updateBestScore(userId, 1, 100);
        bestResultService.updateBestScore(userId, 2, 150);

        Integer overall = bestResultService.getBestOverallScore(userId);
        assertNotNull(overall);
        assertEquals(250, overall);
    }

    @Test
    public void getBestTimesByUserIdTest() {
        int userId = createUser();

        bestResultService.updateBestTime(userId, 1, 4000L);
        bestResultService.updateBestTime(userId, 2, 6000L);

        Map<Integer, Long> times = bestResultService.getBestTimesByUserId(userId);
        assertEquals(2, times.size());
        assertEquals(4000L, times.get(1));
        assertEquals(6000L, times.get(2));
    }

    @Test
    public void getTopByScoreTest() {
        int userId = createUser();
        bestResultService.updateBestScore(userId, 1, 500);

        List<UserScore> top = bestResultService.getTopByScore();
        assertNotNull(top);
        assertFalse(top.isEmpty());
    }

    @Test
    public void getNullWhenNoResultTest() {
        int userId = createUser();

        assertNull(bestResultService.getBestTime(userId, 99));
        assertNull(bestResultService.getBestScore(userId, 99));
        assertNull(bestResultService.getBestOverallScore(userId));
    }
}
