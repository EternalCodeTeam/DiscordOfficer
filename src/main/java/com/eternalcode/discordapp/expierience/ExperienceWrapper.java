package com.eternalcode.discordapp.expierience;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edc_experience")
class ExperienceWrapper {

    @DatabaseField(id = true)
    private Long id;

    @DatabaseField(columnName = "points", defaultValue = "0")
    private int points;

    public ExperienceWrapper() {
    }

    public ExperienceWrapper(Long id, int points) {
        this.id = id;
        this.points = points;
    }

    public static ExperienceWrapper from(Experience userPoints) {
        return new ExperienceWrapper(userPoints.getId(), userPoints.getPoints());
    }

    public Experience toUserPoints() {
        return new Experience(this.id, this.points);
    }
}
