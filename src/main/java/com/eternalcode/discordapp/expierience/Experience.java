package com.eternalcode.discordapp.expierience;

public class Experience {

    private final long id;
    private double points;

    public Experience(long id, double points) {
        this.id = id;
        this.points = points;
    }

    public long getId() {
        return this.id;
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
