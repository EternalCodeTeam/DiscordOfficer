package com.eternalcode.discordapp.database.model;

public class User {
    private final Long id;
    private final String username;

    public User(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
