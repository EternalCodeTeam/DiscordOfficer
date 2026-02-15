package com.eternalcode.discordapp.feature.leveling;

public class Level {

    private final long id;

    private int currentLevel;

    public Level(long id, int currentLevel) {
        this.id = id;
        this.currentLevel = currentLevel;
    }

    public long getId() {
        return this.id;
    }

    public int getCurrentLevel() {
        return this.currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
}

