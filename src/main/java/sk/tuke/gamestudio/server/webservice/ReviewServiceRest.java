package sk.tuke.gamestudio.server.webservice;

import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.ReviewService;

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
            @RequestBody Review review
    ) {
        reviewService.addOrUpdateReview(review);
    }

    @GetMapping("/{userId}/review")
    public Review getReview(@PathVariable int userId) {
        return reviewService.getReview(userId);
    }

    @GetMapping("/overall-rating")
    public Float getOverallRating() {
        return reviewService.getOverallRating();
    }
}
