package com.eternalcode.discordapp.user;

public class User {

    private Long id;

    public User(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
