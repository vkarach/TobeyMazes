package sk.tuke.gamestudio.server.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.game.logicalmazes.core.*;
import sk.tuke.gamestudio.service.BestResultService;
import sk.tuke.gamestudio.service.ReviewService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class TobeyMazesController {
    private final WebGameSession session;
    private final BestResultService bestResultService;
    private final ReviewService reviewService;

    public TobeyMazesController(WebGameSession session, BestResultService bestResultService, ReviewService reviewService) {
        this.session = session;
        this.bestResultService = bestResultService;
        this.reviewService = reviewService;
    }

    @RequestMapping("/menu")
    public String menu(Model model) {
        model.addAttribute("version", Game.version);
        model.addAttribute("versionLabel", Game.versionLabel);
        Float overall = reviewService.getOverallRating();
        List<sk.tuke.gamestudio.entity.Review> all = reviewService.getAllReviews();
        int reviewCount = all != null ? all.size() : 0;
        model.addAttribute("overallRating", overall != null ? overall : 0f);
        model.addAttribute("reviewCount", reviewCount);
        return "menu";
    }

    @GetMapping("/game/levels")
    public String levels(Model model) {
        model.addAttribute("levels", Level.values());
        Integer currentUserId = session.getCurrentUser() != null ? session.getCurrentUser().getId() : null;
        model.addAttribute("currentUserId", currentUserId);
        return "levels";
    }

    @PostMapping("/game/start")
    public String startGame(@RequestParam int levelId) {
        for (Level l : Level.values()) {
            if (l.getId() == levelId) {
                session.setCurrentLevel(l);
                return "redirect:/game";
            }
        }
        return "redirect:/game/levels";
    }

    @RequestMapping(value = "/game/restart", method = {RequestMethod.GET, RequestMethod.POST})
    public String restartGame() {
        if (session.getCurrentLevel() == null) return "redirect:/game/levels";
        return "redirect:/game";
    }

    @GetMapping("/game")
    public String game(Model model) {
        if (session.getCurrentLevel() == null) return "redirect:/game/levels";

        // Reset on every GET — covers F5, restart redirect, and start redirect
        MapParser.Result parsed = new MapParser().parseMap(session.getCurrentLevel().getFilepath());
        session.setField(parsed.mapField());
        session.setPlayer(parsed.player());
        session.setTargetCount(parsed.targetCount());
        session.setStepCount(0);
        session.setGameWon(false);
        session.setStartTimeMs(0);
        session.setLastScore(null);
        session.setLastTimeMs(null);
        session.setTimeRecord(false);
        session.setScoreRecord(false);

        Integer currentUserId = session.getCurrentUser() != null ? session.getCurrentUser().getId() : null;
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("cells", buildCells());
        model.addAttribute("playerX", session.getPlayer().getX());
        model.addAttribute("playerY", session.getPlayer().getY());
        model.addAttribute("stepCount", 0);
        model.addAttribute("gameWon", false);
        model.addAttribute("levelTitle", session.getCurrentLevel().getTitle());
        model.addAttribute("lastScore", null);
        model.addAttribute("lastTimeMs", null);
        model.addAttribute("timeRecord", false);
        model.addAttribute("scoreRecord", false);
        model.addAttribute("difficulty", session.getCurrentLevel().getDifficulty().name());
        return "game";
    }

    @PostMapping("/game/move")
    @ResponseBody
    public Map<String, Object> move(@RequestParam String direction) {
        Map<String, Object> result = new HashMap<>();

        if (session.isGameWon() || session.getField() == null) {
            result.put("path", List.of());
            result.put("collectedTargets", List.of());
            result.put("stepCount", session.getStepCount());
            result.put("gameWon", true);
            result.put("lastTimeMs", session.getLastTimeMs());
            result.put("lastScore", session.getLastScore());
            result.put("timeRecord", session.isTimeRecord());
            result.put("scoreRecord", session.isScoreRecord());
            return result;
        }

        Field field = session.getField();
        Player player = session.getPlayer();
        Direction dir = Direction.valueOf(direction);

        List<int[]> path = new ArrayList<>();
        List<int[]> collectedTargets = new ArrayList<>();

        while (field.canStep(player, dir)) {
            field.step(player, dir);
            path.add(new int[]{player.getX(), player.getY()});
            if (field.takeTarget(player)) {
                collectedTargets.add(new int[]{player.getX(), player.getY()});
                session.setTargetCount(session.getTargetCount() - 1);
            }
        }

        if (!path.isEmpty()) {
            if (session.getStartTimeMs() == 0) {
                session.setStartTimeMs(System.currentTimeMillis());
            }
            session.setStepCount(session.getStepCount() + 1);
        }

        if (session.getTargetCount() == 0) {
            session.setGameWon(true);
            long timeMs = System.currentTimeMillis() - session.getStartTimeMs();
            int score = computePoints(timeMs, session.getStepCount(), session.getCurrentLevel().getDifficulty());
            session.setLastTimeMs(timeMs);
            session.setLastScore(score);

            long timeImproveMs = 0;
            int scoreImprove = 0;

            if (session.getCurrentUser() != null) {
                int userId = session.getCurrentUser().getId();
                int levelId = session.getCurrentLevel().getId();

                Long prevBestTime = bestResultService.getBestTime(userId, levelId);
                Integer prevBestScore = bestResultService.getBestScore(userId, levelId);

                session.setTimeRecord(prevBestTime == null || timeMs < prevBestTime);
                session.setScoreRecord(prevBestScore == null || score > prevBestScore);

                if (session.isTimeRecord()) {
                    timeImproveMs = prevBestTime != null ? prevBestTime - timeMs : 0;
                    bestResultService.updateBestTime(userId, levelId, timeMs);
                }
                if (session.isScoreRecord()) {
                    scoreImprove = prevBestScore != null ? score - prevBestScore : 0;
                    bestResultService.updateBestScore(userId, levelId, score);
                }
            }

            result.put("lastScore", session.getLastScore());
            result.put("lastTimeMs", session.getLastTimeMs());
            result.put("timeRecord", session.isTimeRecord());
            result.put("scoreRecord", session.isScoreRecord());
            result.put("timeImproveMs", timeImproveMs);
            result.put("scoreImprove", scoreImprove);
        }

        result.put("path", path);
        result.put("collectedTargets", collectedTargets);
        result.put("stepCount", session.getStepCount());
        result.put("gameWon", session.isGameWon());
        return result;
    }

    private int computePoints(long timeMs, int stepCount, Level.Difficulty difficulty) {
        int maxPoints, kTime, kStep;
        float timeSec = timeMs / 1000f;
        switch (difficulty) {
            case EASY   -> { maxPoints = 500;  kTime = 12;  kStep = 8; }
            case NORMAL -> { maxPoints = 1000; kTime = 25;  kStep = 15; }
            case MEDIUM -> { maxPoints = 2000; kTime = 44;  kStep = 20; }
            case HARD   -> { maxPoints = 5000; kTime = 69;  kStep = 35; }
            default     -> { return 0; }
        }
        return Math.max(0, maxPoints - stepCount * kStep - (int)(timeSec * kTime));
    }


    // Each cell carries its 4 wall flags and content symbol
    public record CellView(boolean top, boolean bottom, boolean left, boolean right, String content) {}

    private List<List<CellView>> buildCells() {
        Field field = session.getField();
        boolean[][] vWalls = field.getVWalls();
        boolean[][] hWalls = field.getHWalls();
        Tile[][] tiles = field.getTiles();
        int rows = field.getRowCount();
        int cols = field.getColCount();

        List<List<CellView>> grid = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            List<CellView> rowList = new ArrayList<>();
            for (int col = 0; col < cols; col++) {
                boolean wallTop    = hWalls[row][col];
                boolean wallBottom = hWalls[row + 1][col];
                boolean wallLeft   = vWalls[row][col];
                boolean wallRight  = vWalls[row][col + 1];

                String content = tiles[row][col].getType() == TileType.TARGET ? "T" : "";

                rowList.add(new CellView(wallTop, wallBottom, wallLeft, wallRight, content));
            }
            grid.add(rowList);
        }
        return grid;
    }
}
