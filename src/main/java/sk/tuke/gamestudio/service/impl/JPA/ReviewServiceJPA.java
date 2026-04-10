package sk.tuke.gamestudio.service.impl.JPA;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.repository.ReviewRepository;
import sk.tuke.gamestudio.service.ReviewService;

import java.time.LocalDateTime;
import java.util.List;
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
    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByRatingDescUpdatedAtDesc();
    }

    @Override
    public Float getOverallRating() {
        Double avg = reviewRepository.getOverallRating();
        return avg != null ? avg.floatValue() : 0f;
    }

}
