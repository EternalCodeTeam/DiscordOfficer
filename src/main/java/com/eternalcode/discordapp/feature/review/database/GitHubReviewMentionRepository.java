package com.eternalcode.discordapp.feature.review.database;

import com.eternalcode.discordapp.feature.review.GitHubPullRequest;
import com.eternalcode.discordapp.feature.review.GitHubReviewStatus;
import com.eternalcode.discordapp.feature.review.GitHubReviewMention;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GitHubReviewMentionRepository {

    CompletableFuture<Void> markReviewerAsMentioned(GitHubPullRequest pullRequest, long userId, long threadId);

    CompletableFuture<Boolean> isMentioned(GitHubPullRequest pullRequest, long userId);

    CompletableFuture<Void> recordReminderSent(GitHubPullRequest pullRequest, long userId);

    List<ReviewerReminder> getReviewersNeedingReminders(java.time.Duration reminderInterval);

    CompletableFuture<GitHubReviewMention> find(String pullRequest, long userId);

    void updateReviewStatus(GitHubPullRequest pullRequest, GitHubReviewStatus status);

    record ReviewerReminder(long userId, String pullRequestUrl, long threadId) {
    }
}

