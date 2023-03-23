package com.eternalcode.discordapp.review.pr;

public class PullRequestInfo {

    private final String owner;
    private final String repo;
    private final int number;

    public PullRequestInfo(String owner, String repo, int number) {
        this.owner = owner;
        this.repo = repo;
        this.number = number;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public int getNumber() {
        return number;
    }

}