package com.eternalcode.discordapp.leveling.experience;

public class Experience {

    private final long userId;
    private double points;

    public Experience(long userId, double points) {
        this.userId = userId;
        this.points = points;
    }

    public long getUserId() {
        return this.userId;
    }

    public double getPoints() {
        return this.points;
    }

    public void addPoints(double points) {
        this.points += points;
    }

    public void removePoints(double points) {
        this.points -= points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

}
