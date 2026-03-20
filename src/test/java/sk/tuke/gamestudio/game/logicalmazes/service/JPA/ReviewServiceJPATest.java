package sk.tuke.gamestudio.game.logicalmazes.service.JPA;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.ReviewService;
import sk.tuke.gamestudio.service.UserService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewServiceJPATest extends BaseJPATest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Test
    public void addOrUpdateReviewTest() {
        int userId = userService.createUser(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID() + "@test.com"
        );

        Review review = new Review(userId, 1, "Test review");
        reviewService.addOrUpdateReview(review);

        Review fromDb = reviewService.getReview(userId);
        assertNotNull(fromDb);
        assertEquals(review.getUserId(), fromDb.getUserId());
        assertEquals(review.getRating(), fromDb.getRating());
        assertEquals(review.getComment(), fromDb.getComment());

        Review updated = new Review(userId, 5, "Updated review");
        reviewService.addOrUpdateReview(updated);

        fromDb = reviewService.getReview(userId);
        assertEquals(5, fromDb.getRating());
        assertEquals("Updated review", fromDb.getComment());
    }

    @Test
    public void getReviewTest() {
        int userId = userService.createUser(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID() + "@test.com"
        );

        assertNull(reviewService.getReview(userId));

        Review review = new Review(userId, 3, "Some review");
        reviewService.addOrUpdateReview(review);

        Review fromDb = reviewService.getReview(userId);
        assertNotNull(fromDb);
        assertEquals(3, fromDb.getRating());
        assertEquals("Some review", fromDb.getComment());
    }

    @Test
    public void getOverallRatingTest() {
        int userId1 = userService.createUser(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID() + "@test.com"
        );
        int userId2 = userService.createUser(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID() + "@test.com"
        );

        reviewService.addOrUpdateReview(new Review(userId1, 4, "Good"));
        reviewService.addOrUpdateReview(new Review(userId2, 2, "Meh"));

        float rating = reviewService.getOverallRating();
        assertTrue(rating > 0);
    }
}
