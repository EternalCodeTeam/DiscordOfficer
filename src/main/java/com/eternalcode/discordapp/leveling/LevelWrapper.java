package com.eternalcode.discordapp.leveling;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "officer_level")
public class LevelWrapper {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField(columnName = "level", defaultValue = "0")
    private int level;

    public LevelWrapper() {
    }

    public LevelWrapper(long id, int level) {
        this.id = id;
        this.level = level;
    }

    public Level toLevel() {
        return new Level(this.id, this.level);
    }

    public static LevelWrapper from(Level level) {
        return new LevelWrapper(level.getId(), level.getLevel());
    }
}
