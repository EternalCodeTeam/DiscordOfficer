package com.eternalcode.discordapp.review;

public class GitHubPullRequestInfo {

    private final String owner;
    private final String repo;
    private final int number;

    public GitHubPullRequestInfo(String owner, String repo, int number) {
        this.owner = owner;
        this.repo = repo;
        this.number = number;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getRepo() {
        return this.repo;
    }

    public int getNumber() {
        return this.number;
    }

}