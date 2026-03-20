package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "levels")
public class LevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
