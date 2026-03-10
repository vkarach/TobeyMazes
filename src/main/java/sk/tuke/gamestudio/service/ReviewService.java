package sk.tuke.gamestudio.service;


import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.exception.ReviewException;

public interface ReviewService {
    void addOrUpdateReview(Review review) throws ReviewException;
    Review getReview(int userId) throws ReviewException;
    float getOverallRating() throws ReviewException;
}
