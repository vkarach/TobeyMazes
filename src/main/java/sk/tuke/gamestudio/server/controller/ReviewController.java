package sk.tuke.gamestudio.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.ReviewService;
import sk.tuke.gamestudio.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/review")
public class ReviewController {
    private final WebGameSession session;
    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(WebGameSession session, ReviewService reviewService, UserService userService) {
        this.session = session;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping
    public String review(Model model) {
        model.addAttribute("user", session.getCurrentUser());
        model.addAttribute("overallRating", reviewService.getOverallRating());

        Review review = null;
        if (session.getCurrentUser() != null) {
            review = reviewService.getReview(session.getCurrentUser().getId());
        }
        model.addAttribute("review", review);

        return "review";
    }

    @GetMapping("/all/ajax")
    @ResponseBody
    public List<Map<String, Object>> allReviewsAjax() {
        List<Review> reviews = reviewService.getAllReviews();
        if (reviews == null) return List.of();
        return reviews.stream().map(r -> {
            String name = userService.getUserNameById(r.getUserId());
            LocalDateTime date = r.getUpdatedAt();
            if (date == null) date = userService.getCreatedAt(r.getUserId());
            return Map.<String, Object>of(
                "userName", name != null ? name : "Unknown",
                "rating", r.getRating(),
                "comment", r.getComment() != null ? r.getComment() : "",
                "date", date != null ? date.toString() : ""
            );
        }).toList();
    }

    @PostMapping("/submit/ajax")
    @ResponseBody
    public Map<String, Object> submitReviewAjax(
        @RequestParam int rating,
        @RequestParam String review
    ) {
        if (session.getCurrentUser() == null) return Map.of("error", "Not logged in");
        reviewService.addOrUpdateReview(
                new Review(session.getCurrentUser().getId(), rating, review)
        );
        float overall = reviewService.getOverallRating();
        return Map.of("ok", true, "overallRating", overall);
    }

    @PostMapping("/submit")
    public String submitReview(
        @RequestParam int rating,
        @RequestParam String review
    ) {
        reviewService.addOrUpdateReview(
                new Review(session.getCurrentUser().getId(), rating, review)
        );
        return "redirect:/review";
    }
}
