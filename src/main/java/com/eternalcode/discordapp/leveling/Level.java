package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.experience.Experience;

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
