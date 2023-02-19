package com.eternalcode.discordapp.database.model;

public class UserPoints {
    private Long id;
    private Long points;
    private final User user;

    public UserPoints(Long id, Long points, User user) {
        this.id = id;
        this.points = points;
        this.user = user;
    }

    public Long getId() {
        return this.id;
    }

    public Long getPoints() {
        return this.points;
    }

    public User getUser() {
        return this.user;
    }

    public void addPoints(Long points) {
        this.points += points;
    }

    public void removePoints(Long points) {
        this.points -= points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }
}
