package com.eternalcode.discordapp.feature.review.database;

import com.eternalcode.discordapp.feature.review.GitHubReviewMention;
import com.eternalcode.discordapp.feature.review.GitHubReviewStatus;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.Instant;
import java.util.Objects;

@DatabaseTable(tableName = "github_review_mentions")
public final class GitHubReviewMentionWrapper {

    private static final String KEY_SEPARATOR = "|";

    @DatabaseField(id = true)
    private String pullRequest;

    @DatabaseField
    private long userId;

    @DatabaseField
    private long lastMention;

    @DatabaseField
    private String reviewStatus;

    @DatabaseField
    private long threadId;

    @DatabaseField
    private long lastReminderSent;

    public GitHubReviewMentionWrapper() {
        // ORMLite requires a no-arg constructor
    }

    public GitHubReviewMentionWrapper(
        String pullRequest,
        long userId,
        long lastMention,
        String reviewStatus,
        long threadId,
        long lastReminderSent) {
        this.pullRequest = pullRequest;
        this.userId = userId;
        this.lastMention = lastMention;
        this.reviewStatus = reviewStatus;
        this.threadId = threadId;
        this.lastReminderSent = lastReminderSent;
    }

    public static GitHubReviewMentionWrapper create(
        String pullRequest,
        long userId,
        Instant lastMention,
        GitHubReviewStatus reviewStatus,
        long threadId) {
        return new GitHubReviewMentionWrapper(
            pullRequest,
            userId,
            lastMention.toEpochMilli(),
            reviewStatus.name(),
            threadId,
            0
        );
    }

    public String getPullRequest() {
        return this.pullRequest;
    }

    public String getPullRequestUrl() {
        int separatorIndex = this.pullRequest.lastIndexOf(KEY_SEPARATOR);
        if (separatorIndex <= 0) {
            return this.pullRequest;
        }
        return this.pullRequest.substring(0, separatorIndex);
    }

    public long getUserId() {
        return this.userId;
    }

    public Instant getLastMention() {
        return Instant.ofEpochMilli(this.lastMention);
    }

    public long getThreadId() {
        return this.threadId;
    }

    public void setLastMention(Instant lastMention) {
        this.lastMention = lastMention == null ? 0 : lastMention.toEpochMilli();
    }

    public void setReviewStatus(GitHubReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus == null ? GitHubReviewStatus.PENDING.name() : reviewStatus.name();
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public void setLastReminderSent(Instant lastReminderSent) {
        this.lastReminderSent = lastReminderSent == null ? 0 : lastReminderSent.toEpochMilli();
    }

    public GitHubReviewMention toMention() {
        return new GitHubReviewMention(
            this.getPullRequestUrl(),
            this.userId,
            this.lastMention,
            this.reviewStatus,
            this.threadId,
            this.lastReminderSent
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GitHubReviewMentionWrapper that)) {
            return false;
        }
        return this.userId == that.userId &&
            this.pullRequest != null &&
            this.pullRequest.equals(that.pullRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pullRequest, this.userId);
    }

    @Override
    public String toString() {
        return "GitHubReviewMentionWrapper{" +
            "pullRequest='" + pullRequest + '\'' +
            ", userId=" + userId +
            ", lastMention=" + lastMention +
            ", reviewStatus='" + reviewStatus + '\'' +
            ", threadId=" + threadId +
            ", lastReminderSent=" + lastReminderSent +
            '}';
    }
}

