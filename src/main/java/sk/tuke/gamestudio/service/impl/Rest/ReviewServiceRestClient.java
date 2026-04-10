package sk.tuke.gamestudio.service.impl.Rest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.config.RestClientConfig;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.ReviewService;

import java.util.List;

@Profile({"console", "fxgl"})
@Service
public class ReviewServiceRestClient implements ReviewService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ReviewServiceRestClient(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.baseUrl = restClientConfig.getBaseUrl() + "/api/users";
    }

    @Override
    public void addOrUpdateReview(Review review) {
        String url = String.format(
                "%s/%d/review", baseUrl, review.getUserId()
        );
        restTemplate.put(url, review);
    }

    @Override
    public Review getReview(int userId) {
        String url = String.format(
                "%s/%d/review", baseUrl, userId
        );
        return restTemplate.getForObject(url, Review.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Review> getAllReviews() {
        String url = String.format(
          "%s/reviews", baseUrl
        );
        return restTemplate.getForObject(url, List.class);
    }

    @Override
    public Float getOverallRating() {
        String url = String.format(
          "%s/overall-rating", baseUrl
        );
        return restTemplate.getForObject(url, Float.class);
    }
}
