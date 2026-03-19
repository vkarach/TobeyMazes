package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "best_level_results")
public class BestLevelResult {

    @EmbeddedId
    private BestLevelResultId id;

    @Column(name = "best_time_ms")
    private Long bestTimeMs;

    @Column
    private Integer bestScore;

//    @Column(name = "achieved_at", nullable = false)
//    private LocalDateTime achieved_at;

    protected BestLevelResult() {}

    public BestLevelResult(int userId, int levelId) {
        id = new BestLevelResultId(userId, levelId);
    }

    public BestLevelResultId getId() {
        return id;
    }

    public Long getBestTimeMs() {
        return bestTimeMs;
    }

    public Integer getBestScore() {
        return bestScore;
    }

    public void setBestTimeMs(Long bestTimeMs) {
        this.bestTimeMs = bestTimeMs;
    }

    public void setBestScore(Integer bestScore) {
        this.bestScore = bestScore;
    }
}
