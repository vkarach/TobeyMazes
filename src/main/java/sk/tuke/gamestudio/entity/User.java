package sk.tuke.gamestudio.entity;

public class User {
    private final String name;
    private final int id;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
