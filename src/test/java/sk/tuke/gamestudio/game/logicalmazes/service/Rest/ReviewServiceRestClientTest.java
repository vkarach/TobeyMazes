package sk.tuke.gamestudio.game.logicalmazes.service.Rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.impl.Rest.ReviewServiceRestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class ReviewServiceRestClientTest extends BaseRestClientTest {

    private ReviewServiceRestClient reviewService;

    @BeforeEach
    void init() {
        reviewService = new ReviewServiceRestClient(restTemplate, restClientConfig);
    }

    @Test
    void addOrUpdateReviewTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/1/review"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        Review review = new Review(1, 5, "Great game");
        assertDoesNotThrow(() -> reviewService.addOrUpdateReview(review));
        mockServer.verify();
    }

    @Test
    void getReviewTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/1/review"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"userId\":1,\"rating\":5,\"comment\":\"Great game\"}",
                        MediaType.APPLICATION_JSON
                ));

        Review review = reviewService.getReview(1);
        assertNotNull(review);
        assertEquals(1, review.getUserId());
        assertEquals(5, review.getRating());
        mockServer.verify();
    }

    @Test
    void getOverallRatingTest() {
        mockServer.expect(requestTo(BASE_URL + "/api/users/overall-rating"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("4.5", MediaType.APPLICATION_JSON));

        assertEquals(4.5f, reviewService.getOverallRating());
        mockServer.verify();
    }
}
