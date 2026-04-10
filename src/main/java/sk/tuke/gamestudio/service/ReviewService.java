package sk.tuke.gamestudio.service;


import sk.tuke.gamestudio.entity.Review;
import sk.tuke.gamestudio.service.exception.ReviewException;

import java.util.List;

public interface ReviewService {
    void addOrUpdateReview(Review review) throws ReviewException;
    Review getReview(int userId) throws ReviewException;
    Float getOverallRating() throws ReviewException;
    default List<Review> getAllReviews() throws ReviewException { return null; }
}
