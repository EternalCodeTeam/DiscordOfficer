package com.eternalcode.discordapp.feature.review;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import panda.std.Result;

public class GitHubPullRequest {

    private static final Pattern GITHUB_PULL_REQUEST_REGEX =
        Pattern.compile("^https://github\\.com/([a-zA-Z0-9-_]+)/([a-zA-Z0-9-_]+)/pull/([0-9]+)$");

    private static final String GITHUB_PULL_REQUEST_URL = "https://github.com/%s/%s/pull/%s";
    private static final String GITHUB_PULL_REQUEST_API_URL = "https://api.github.com/repos/%s/%s/pulls/%d";

    private final String owner;
    private final String repository;
    private final int number;

    public GitHubPullRequest(String owner, String repository, int number) {
        this.owner = owner;
        this.repository = repository;
        this.number = number;
    }

    public static Result<GitHubPullRequest, IllegalArgumentException> fromUrl(String reviewUrl) {
        Matcher matcher = GITHUB_PULL_REQUEST_REGEX.matcher(reviewUrl);

        if (!matcher.matches()) {
            return Result.error(new IllegalArgumentException("Invalid GitHub pull request URL"));
        }

        String owner = matcher.group(1);
        String repository = matcher.group(2);
        int number = Integer.parseInt(matcher.group(3));

        return Result.ok(new GitHubPullRequest(owner, repository, number));
    }

    public String getOwner() {
        return this.owner;
    }

    public String getRepository() {
        return this.repository;
    }

    public int getNumber() {
        return this.number;
    }

    public String toUrl() {
        return String.format(GITHUB_PULL_REQUEST_URL, this.owner, this.repository, this.number);
    }

    public String toApiUrl() {
        return String.format(GITHUB_PULL_REQUEST_API_URL, this.owner, this.repository, this.number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof GitHubPullRequest that)) {
            return false;
        }

        return this.number == that.number && Objects.equals(this.owner, that.owner) && Objects.equals(
            this.repository,
            that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.owner, this.repository, this.number);
    }
}

