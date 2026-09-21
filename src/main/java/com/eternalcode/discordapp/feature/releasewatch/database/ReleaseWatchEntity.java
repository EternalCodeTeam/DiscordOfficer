package com.eternalcode.discordapp.feature.releasewatch.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "release_watches")
public final class ReleaseWatchEntity {

    @DatabaseField(id = true)
    private String repoId;

    @DatabaseField
    private String displayName;

    @DatabaseField
    private String lastReleaseTag;

    public ReleaseWatchEntity() {
        // ORMLite requires a no-arg constructor
    }

    public ReleaseWatchEntity(String repoId, String displayName, String lastReleaseTag) {
        this.repoId = repoId;
        this.displayName = displayName;
        this.lastReleaseTag = lastReleaseTag;
    }

    public String getRepoId() {
        return this.repoId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getLastReleaseTag() {
        return this.lastReleaseTag;
    }

    public void setLastReleaseTag(String lastReleaseTag) {
        this.lastReleaseTag = lastReleaseTag;
    }
}
