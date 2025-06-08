package com.eternalcode.discordapp.review;

public record GitHubReviewMention(
    String pullRequest,
    long userId,
    long lastMention,
    String reviewStatus,
    long threadId,
    long lastReminderSent
) {
    // Record automatically provides constructor, getters, equals, hashCode, and toString
}
