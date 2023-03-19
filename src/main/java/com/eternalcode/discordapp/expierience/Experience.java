package com.eternalcode.discordapp.expierience;

public class Experience {
    private final Long id;
    private int points;

    public Experience(Long id, int points) {
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
