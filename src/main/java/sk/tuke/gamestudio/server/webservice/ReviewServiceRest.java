package sk.tuke.gamestudio.server.webservice;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class ReviewServiceRest {
    private final ReviewService reviewService;

    public ReviewServiceRest(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PutMapping("/{userId}/review")
    public void addOrUpdateReview(
            @PathVariable int userId,
            @RequestBody Review review,
            Authentication auth
    ) {
        int tokenUserId = (Integer) auth.getPrincipal();
        if (tokenUserId != userId) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        reviewService.addOrUpdateReview(review);
    }

    @GetMapping("/{userId}/review")
    public Review getReview(@PathVariable int userId) {
        return reviewService.getReview(userId);
    }

    @GetMapping("/reviews")
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/overall-rating")
    public Float getOverallRating() {
        return reviewService.getOverallRating();
    }
}
