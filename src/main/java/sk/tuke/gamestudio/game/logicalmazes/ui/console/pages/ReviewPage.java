package sk.tuke.gamestudio.game.logicalmazes.ui.console.pages;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.ConsoleRenderer;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.InputHelper;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Notifier;
import sk.tuke.gamestudio.game.logicalmazes.ui.console.Selector;
import sk.tuke.gamestudio.game.logicalmazes.utils.SoundUtil;
import sk.tuke.gamestudio.service.ReviewService;

@Component
public class ReviewPage {
    private final Console console;
    private final ConsoleRenderer consoleRenderer;
    private final Selector selector;
    private final InputHelper inputHelper;
    private final Notifier notifier;

    private final SoundUtil confirmSound = new SoundUtil("sounds/confirm.wav");

    public ReviewPage(Console console, ConsoleRenderer consoleRenderer, Selector selector, InputHelper inputHelper, Notifier notifier) {
        this.console = console;
        this.consoleRenderer = consoleRenderer;
        this.selector = selector;
        this.inputHelper = inputHelper;
        this.notifier = notifier;
    }

    public void show(User currentUser, ReviewService reviewService) {
        console.clear();
        consoleRenderer.renderFromFile("uiTexts/rate_title.txt");
        consoleRenderer.renderFromFile("uiTexts/stars.txt", 120, 0);
        consoleRenderer.renderFromFile("uiTexts/stars.txt", 120, 15);

        int x = Selector.DEFAULT_X;
        int y = 21;

        printRating(reviewService, 65, y);

        if (currentUser == null) {
            console.print("Sign in to rate the game", x, y);
            inputHelper.waitForConfirm("Back", x, 30);
            return;
        }

        Review review = reviewService.getReview(currentUser.getId());
        if (review != null) {
            String reviewText = String.format("%d★ %s", review.getRating(),
                    !review.getComment().isEmpty() ? review.getComment() : "(without comment)");
            console.print("You already rated the game:", x, y);
            console.print(reviewText, x, y + 1);
            String selected = selector.select(new String[]{"Edit", "Back"}, x, y + 3);
            if (selected == null || selected.equals("Back")) return;
        }

        console.print("Rate the game ←→", x, y - 1);
        for (int i = 0; i < 5; i++) console.clearLine(x, y + i);

        Integer ratingValue = selector.selectRating(x, y);
        if (ratingValue == null) return;

        String commentText;
        while (true) {
            commentText = inputHelper.getUserInput("Comment (optional): ", x, y + 1);
            if (commentText == null) { commentText = ""; }
            if (commentText.isEmpty()) break;
            String error = inputHelper.validateInput(commentText, null, 6, 100);
            if (error != null) { notifier.showError(error, x, y + 2); continue; }
            break;
        }

        reviewService.addOrUpdateReview(new Review(currentUser.getId(), ratingValue, commentText));
        console.clearLine(x, y + 1);
        console.print(commentText, x, y + 1);
        printRating(reviewService, 65, y);
        confirmSound.play();
        console.print("Thank you for your feedback!", x, y + 3);
        inputHelper.waitForConfirm("Back", x, y + 5);
    }

    private void printRating(ReviewService reviewService, int x, int y) {
        float overallRating = reviewService.getOverallRating();
        String ratingText = String.format("Overall rating: %s",
                overallRating > 0 ? String.format("%.2f", overallRating) : "no one rated yet");
        console.print(ratingText, x, y);
    }
}