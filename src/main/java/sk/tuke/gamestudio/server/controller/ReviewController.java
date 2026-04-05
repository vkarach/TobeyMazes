package sk.tuke.gamestudio.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.ReviewService;

@Controller
@RequestMapping("/review")
public class ReviewController {
    private final WebGameSession session;
    private final ReviewService reviewService;

    public ReviewController(WebGameSession session, ReviewService reviewService) {
        this.session = session;
        this.reviewService = reviewService;
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

    @GetMapping("/submit")
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
