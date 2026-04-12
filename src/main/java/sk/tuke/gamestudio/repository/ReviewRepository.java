package sk.tuke.gamestudio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sk.tuke.gamestudio.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByUserId(int userId);

    List<Review> findAll();

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getOverallRating();
}
