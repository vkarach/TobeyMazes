package sk.tuke.gamestudio.game.logicalmazes.service;

import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.UserService;
import sk.tuke.gamestudio.service.impl.ReviewServiceJDBC;
import sk.tuke.gamestudio.service.ReviewService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.service.impl.UserServiceJDBC;

import java.util.UUID;

public class ReviewServiceTest {
    ReviewService reviewService;
    private final UserService userService;

    public ReviewServiceTest () {
        this.reviewService = new ReviewServiceJDBC();
        this.userService = new UserServiceJDBC();
    }

    @Test
    public void addOrUpdateReviewTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password);

        assertTrue(userService.userExists(userName));

        Review review = new Review(userId, 1, "Test review");
        reviewService.addOrUpdateReview(review);

        Review reviewFromDb = reviewService.getReview(userId);

        checkReviewsIdentically(review, reviewFromDb);

        Review reviewNew = new Review(userId, 5, "Test new review");
        reviewService.addOrUpdateReview(reviewNew);

        reviewFromDb = reviewService.getReview(userId);

        checkReviewsIdentically(reviewNew, reviewFromDb);

        userService.deleteUserByName(userName);
    }

    @Test
    public void getReviewTest() {
        String userName = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        int userId = userService.createUser(userName, password);

        assertTrue(userService.userExists(userName));

        Review review = new Review(userId, 1, "Test review");
        reviewService.addOrUpdateReview(review);

        Review reviewFromDb = reviewService.getReview(userId);

        checkReviewsIdentically(review, reviewFromDb);

        userService.deleteUserByName(userName);
    }

    private void checkReviewsIdentically(Review review1, Review review2) {
        assertEquals(review1.userId(), review2.userId());
        assertEquals(review1.rating(), review2.rating());
        assertEquals(review1.comment(), review2.comment());
    }
}
