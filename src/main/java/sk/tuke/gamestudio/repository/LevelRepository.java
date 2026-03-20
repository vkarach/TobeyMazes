package sk.tuke.gamestudio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.tuke.gamestudio.entity.LevelEntity;

public interface LevelRepository extends JpaRepository<LevelEntity, Integer> {
}
