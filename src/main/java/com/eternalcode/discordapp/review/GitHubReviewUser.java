package com.eternalcode.discordapp.review;

import net.dzikoysk.cdn.entity.Contextual;

@Contextual
public class GitHubReviewUser {

    private Long discordId;
    private String githubUsername;

    public GitHubReviewUser(Long id, String username) {
        this.discordId = id;
        this.githubUsername = username;
    }

    public GitHubReviewUser() {}

    public Long discordId() {
        return this.discordId;
    }

    public String githubUsername() {
        return this.githubUsername;
    }

}