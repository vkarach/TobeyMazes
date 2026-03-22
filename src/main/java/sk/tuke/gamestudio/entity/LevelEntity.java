package sk.tuke.gamestudio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "levels")
public class LevelEntity {

    @Id
    @Column(name = "level_id")
    private Integer id;

    @Column(name = "level_name")
    private String name;

    protected LevelEntity() {}

    public LevelEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

}
