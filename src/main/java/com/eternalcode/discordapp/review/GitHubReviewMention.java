package com.eternalcode.discordapp.review;

public class GitHubReviewMention {
    private final String pullRequest;
    private final long userId;
    private final long lastMention;
    private final String reviewStatus;
    private final long threadId;
    private final long lastReminderSent;

    public GitHubReviewMention(
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

    public String getPullRequest() {
        return pullRequest;
    }

    public long getUserId() {
        return userId;
    }

    public long getLastMention() {
        return lastMention;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public long getThreadId() {
        return threadId;
    }

    public long getLastReminderSent() {
        return lastReminderSent;
    }
}
