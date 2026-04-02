package sk.tuke.gamestudio.service.impl.JPA;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.repository.ReviewRepository;
import sk.tuke.gamestudio.service.ReviewService;
import sk.tuke.gamestudio.service.exception.ReviewException;

import java.time.LocalDateTime;
import java.util.Optional;

@Profile("server")
@Service
public class ReviewServiceJPA implements ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewServiceJPA(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void addOrUpdateReview(Review review) {
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    @Override
    public Review getReview(int userId) {
        Optional<Review> review =  reviewRepository.findByUserId(userId);
        return review.orElse(null);
    }

    @Override
    public Float getOverallRating() {
        return reviewRepository.getOverallRating().floatValue();
    }
}
