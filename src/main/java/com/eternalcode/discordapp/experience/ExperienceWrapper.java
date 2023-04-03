package com.eternalcode.discordapp.experience;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "officer_experience")
class ExperienceWrapper {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField(columnName = "points", defaultValue = "0")
    private double points;

    public ExperienceWrapper() {
    }

    public ExperienceWrapper(long id, double points) {
        this.id = id;
        this.points = points;
    }

    public static ExperienceWrapper from(Experience userPoints) {
        return new ExperienceWrapper(userPoints.getId(), userPoints.getPoints());
    }

    public Experience toExperience() {
        return new Experience(this.id, this.points);
    }
}
