package com.eternalcode.discordapp.leveling;

public class Level {

    private final long id;

    private int level;

    public Level(long id, int level) {
        this.id = id;
        this.level = level;
    }

    public long getId() {
        return this.id;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
