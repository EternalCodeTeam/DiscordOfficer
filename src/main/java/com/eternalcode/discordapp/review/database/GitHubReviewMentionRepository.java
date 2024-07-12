package com.eternalcode.discordapp.review.database;

import com.eternalcode.discordapp.review.GitHubPullRequest;
import java.util.concurrent.CompletableFuture;

public interface GitHubReviewMentionRepository {

    CompletableFuture<Void> markReviewerAsMentioned(GitHubPullRequest pullRequest, long userId);

    CompletableFuture<Boolean> isMentioned(GitHubPullRequest pullRequest, long userId);

}
