package com.eternalcode.discordapp.review.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.Instant;

@DatabaseTable(tableName = "review_mentions")
public final class GitHubReviewMentionWrapper {

    @DatabaseField(id = true)
    private String pullRequest;

    @DatabaseField
    private long userId;

    @DatabaseField
    private long lastMention;

    public GitHubReviewMentionWrapper() {
    }

    private GitHubReviewMentionWrapper(String pullRequest, long userId, long lastMention) {
        this.pullRequest = pullRequest;
        this.userId = userId;
        this.lastMention = lastMention;
    }

    public static GitHubReviewMentionWrapper create(String pullRequest, long userId, Instant lastMention) {
        return new GitHubReviewMentionWrapper(pullRequest, userId, lastMention.toEpochMilli());
    }

    public String getPullRequest() {
        return this.pullRequest;
    }

    public long getUserId() {
        return this.userId;
    }

    public Instant getLastMention() {
        return Instant.ofEpochMilli(this.lastMention);
    }

}
