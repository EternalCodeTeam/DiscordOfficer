package com.eternalcode.discordapp.review;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

class GitHubReviewMentionRepositoryImpl implements GitHubReviewMentionRepository {

    private final Map<GitHubPullRequest, Map<Long, Instant>> reviewMentions = new HashMap<>();

    @Override
    public void markReviewerAsMentioned(GitHubPullRequest pullRequest, long userId) {
        Map<Long, Instant> userMentions = this.reviewMentions.computeIfAbsent(pullRequest, k -> new HashMap<>());

        userMentions.put(userId, Instant.now());
    }

    @Override
    public boolean isMentioned(GitHubPullRequest pullRequest, long userId) {
        Map<Long, Instant> userMentions = this.reviewMentions.get(pullRequest);

        if (userMentions == null) {
            return false;
        }

        Instant lastMention = userMentions.get(userId);

        if (lastMention == null) {
            return false;
        }

        Instant nextMention = lastMention.plus(8, ChronoUnit.HOURS);

        return nextMention.isAfter(Instant.now());
    }


}
