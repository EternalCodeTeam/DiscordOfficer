package com.eternalcode.discordapp.feature.releasewatch.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "release_watch_links")
public final class ReleaseWatchLinkEntity {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, index = true)
    private String repoId;

    @DatabaseField(canBeNull = false)
    private String platform;

    @DatabaseField(canBeNull = false)
    private String label;

    @DatabaseField(canBeNull = false)
    private String value;

    public ReleaseWatchLinkEntity() {
        // ORMLite requires a no-arg constructor
    }

    public ReleaseWatchLinkEntity(String repoId, String platform, String label, String value) {
        this.repoId = repoId;
        this.platform = platform;
        this.label = label;
        this.value = value;
    }

    public int getId() {
        return this.id;
    }

    public String getRepoId() {
        return this.repoId;
    }

    public String getPlatform() {
        return this.platform;
    }

    public String getLabel() {
        return this.label;
    }

    public String getValue() {
        return this.value;
    }
}
