package com.eternalcode.discordapp.review;

import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Exclude;

@Contextual
public class GitHubReviewUser {

    private Long discordId;
    private String githubUsername;
    private GitHubReviewNotificationType notificationType;

    public GitHubReviewUser(Long id, String username, GitHubReviewNotificationType notificationType) {
        this.discordId = id;
        this.githubUsername = username;
        this.notificationType = notificationType;
    }

    public GitHubReviewUser() {}

    public GitHubReviewNotificationType getNotificationType() {
        return this.notificationType;
    }

    @Exclude
    public void setNotificationType(GitHubReviewNotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Long getDiscordId() {
        return this.discordId;
    }

    public String getGithubUsername() {
        return this.githubUsername;
    }
    
    @Override
    public String toString() {
        return """
            GitHubReviewUser[
                discordId=%s,
                githubUsername=%s,
                notificationType=%s
            ]""".formatted(
                this.discordId,
                this.githubUsername,
                this.notificationType
            );
    }
}