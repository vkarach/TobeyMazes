package sk.tuke.gamestudio.repository;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sk.tuke.gamestudio.entity.BestLevelResult;
import sk.tuke.gamestudio.entity.BestLevelResultId;
import sk.tuke.gamestudio.entity.UserScore;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BestLevelResultRepository extends JpaRepository<BestLevelResult, BestLevelResultId> {
    @NonNull
    Optional<BestLevelResult> findById(@NonNull BestLevelResultId id);

    @Query(
        "SELECT SUM (br.bestScore) " +
        "FROM BestLevelResult br " +
        "WHERE br.id.userId = :userId"
    )
    Optional<Integer> getBestOverallScoreByUserId(int userId);

    @Query(value =
            "SELECT pos FROM (" +
            "  SELECT u.user_id, ROW_NUMBER() OVER (ORDER BY SUM(br.best_score) DESC) AS pos " +
            "  FROM best_level_results br " +
            "  JOIN users u ON u.user_id = br.user_id " +
            "  GROUP BY u.user_id" +
            ") ranked WHERE user_id = :userId",
            nativeQuery = true
    )
    Optional<Integer> getUserLeaderboardPosition(@Param("userId") int userId);

    @Query(
        "SELECT new sk.tuke.gamestudio.entity.UserScore(u.id, u.name, CAST(SUM(br.bestScore) AS integer)) " +
        "FROM BestLevelResult br, User u " +
        "WHERE u.id = br.id.userId " +
        "GROUP BY u.id, u.name " +
        "ORDER BY SUM(br.bestScore) DESC"
    )
    List<UserScore> getTopTenUsers(Pageable pageable);

    @Query("SELECT br FROM BestLevelResult br WHERE br.id.userId = :userId")
    List<BestLevelResult> getBestResultsByUserId(@Param("userId") int userId);
}
