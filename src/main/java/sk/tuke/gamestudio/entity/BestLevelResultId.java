package sk.tuke.gamestudio.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BestLevelResultId  implements Serializable {

    private int userId;
    private int levelId;

    protected BestLevelResultId() {}

    public BestLevelResultId(int userId, int levelId) {
        this.userId = userId;
        this.levelId = levelId;
    }

    public int getUserId() {
        return userId;
    }

    public int getLevelId() {
        return levelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BestLevelResultId other)) return false;
        return userId == other.userId && levelId == other.levelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, levelId);
    }
}
