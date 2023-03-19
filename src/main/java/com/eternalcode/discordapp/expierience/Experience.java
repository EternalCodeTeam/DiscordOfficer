package com.eternalcode.discordapp.expierience;

public class Experience {
    private final long id;
    private int points;

    public Experience(long id, int points) {
        this.id = id;
        this.points = points;
    }

    public long getId() {
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
