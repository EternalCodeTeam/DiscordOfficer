package com.eternalcode.discordapp.database.model;

public class User {
    private Long id;
    private final UserPoints userPoints;

    public User(Long id, UserPoints userPoints) {
        this.id = id;
        this.userPoints = userPoints;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
