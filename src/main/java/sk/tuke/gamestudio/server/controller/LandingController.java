package sk.tuke.gamestudio.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.ReviewService;

import java.util.List;

@Controller
public class LandingController {

    private final ReviewService reviewService;

    public LandingController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String landing(Model model) {
        Float overall = reviewService.getOverallRating();
        List<Review> all = reviewService.getAllReviews();
        int reviewCount = all != null ? all.size() : 0;
        model.addAttribute("overallRating", overall != null ? overall : 0f);
        model.addAttribute("reviewCount", reviewCount);
        return "landing";
    }
}
