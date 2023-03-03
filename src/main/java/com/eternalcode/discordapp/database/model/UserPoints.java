package com.eternalcode.discordapp.database.model;

import java.math.BigInteger;

public class UserPoints {
    private Long id;
    private int points;

    public UserPoints(Long id, int points) {
        this.id = id;
        this.points = points;
    }

    public Long getId() {
        return this.id;
    }

    public int getPoints() {
        return this.points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void removePoints(int points) {
        this.points -= points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
