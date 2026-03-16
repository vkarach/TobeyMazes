package sk.tuke.gamestudio.game.logicalmazes.service;

import sk.tuke.gamestudio.entity.UserScore;
import sk.tuke.gamestudio.service.BestResultService;
import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.impl.BestResultServiceJDBC;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.service.impl.UserServiceJDBC;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BestResultServiceTest {
    private final BestResultService bestResultService;
    private final UserService userService;

    public BestResultServiceTest() {
        this.bestResultService = new BestResultServiceJDBC();
        userService = new UserServiceJDBC();
    }

    @Test
    public void updateAndGetBestTimeTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        int levelId = 1;
        assertNull(bestResultService.getBestTime(userId, levelId));

        int timeMs = 1000;
        bestResultService.updateBestTime(userId, levelId, timeMs);

        assertEquals(timeMs, bestResultService.getBestTime(userId, levelId));

        timeMs = 2000;
        bestResultService.updateBestTime(userId, levelId, timeMs);

        assertEquals(timeMs, bestResultService.getBestTime(userId, levelId));

        userService.deleteUserByName(userName);
    }

    @Test
    public void updateAndGetBestScoreTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        int levelId = 1;
        assertNull(bestResultService.getBestScore(userId, levelId));

        int score = 100;
        bestResultService.updateBestScore(userId, levelId, score);

        assertEquals(score, bestResultService.getBestScore(userId, levelId));

        score = 200;
        bestResultService.updateBestScore(userId, levelId, score);

        assertEquals(score, bestResultService.getBestScore(userId, levelId));

        userService.deleteUserByName(userName);
    }

    @Test
    public void getTopByScoreTest() {
        List<UserScore> expectedBestUsers = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String userName = UUID.randomUUID().toString();
            String password = UUID.randomUUID().toString();

            int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

            assertTrue(userService.userExists(userName));

            int levelId = 1;
            bestResultService.updateBestScore(userId, levelId, 100_000_000 + i);

            expectedBestUsers.add(new UserScore(userId, userName, 100_000_000 + i));
        }

        List<UserScore> actualBestUsers = bestResultService.getTopByScore();

        expectedBestUsers.sort((a, b) -> Integer.compare(b.totalScore(), a.totalScore()));

        assertEquals(expectedBestUsers.size(), actualBestUsers.size());

        for (int i = 0; i < expectedBestUsers.size(); i++) {
            UserScore expected = expectedBestUsers.get(i);
            UserScore actual = actualBestUsers.get(i);

            assertEquals(expected.userId(), actual.userId());
            assertEquals(expected.userName(), actual.userName());
            assertEquals(expected.totalScore(), actual.totalScore());
        }

        for (int i = 0; i < actualBestUsers.size() - 1; i++) {
            assertTrue(
                    actualBestUsers.get(i).totalScore() >= actualBestUsers.get(i + 1).totalScore()
            );
        }

        for (int i = 0; i < 10; i++) {
            userService.deleteUserByName(expectedBestUsers.get(i).userName());
        }
    }

    @Test
    public void getBestScoreWithoutScoreTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        Integer bestScore = bestResultService.getBestScore(userId, 1);
        assertNull(bestScore);

        userService.deleteUserByName(userName);
    }

    @Test
    public void getBestTimeWithoutTimeTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password, UUID.randomUUID() + "@gmail.com ");

        assertTrue(userService.userExists(userName));

        Integer bestTime = bestResultService.getBestTime(userId, 1);
        assertNull(bestTime);

        userService.deleteUserByName(userName);
    }
}
