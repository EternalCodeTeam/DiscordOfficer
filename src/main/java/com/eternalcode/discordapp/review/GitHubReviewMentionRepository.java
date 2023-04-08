package com.eternalcode.discordapp.review;

public interface GitHubReviewMentionRepository {

    void markReviewerAsMentioned(GitHubPullRequest pullRequest, long userId);

    boolean isMentioned(GitHubPullRequest pullRequest, long userId);

}
