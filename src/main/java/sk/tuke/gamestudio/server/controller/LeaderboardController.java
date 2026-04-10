package sk.tuke.gamestudio.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sk.tuke.gamestudio.service.BestResultService;

@Controller
@RequestMapping("/leaderboard")
public class LeaderboardController {
    private final WebGameSession session;
    private final BestResultService bestResultService;

    public LeaderboardController(
            WebGameSession session,
            BestResultService bestResultService
    ) {
        this.session = session;
        this.bestResultService = bestResultService;
    }

    @GetMapping
    public String leaderboard(Model model) {
        model.addAttribute("user", session.getCurrentUser());
        model.addAttribute("topPlayers", bestResultService.getTopByScore());
        if (session.getCurrentUser() != null) {
            int userId = session.getCurrentUser().getId();
            model.addAttribute("userRank", bestResultService.getUserLeaderboardPosition(userId));
            model.addAttribute("userScore", bestResultService.getBestOverallScore(userId));
        }
        return "leaderboard";
    }
}
