package com.eternalcode.discordapp.review;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubPullRequestInfo {

    private static final Pattern PULL_REQUEST_REGEX = Pattern.compile("https://github\\.com/(\\w+)/([\\w-]+)/pull/(\\d+)");

    private final String owner;
    private final String repo;
    private final int number;

    public GitHubPullRequestInfo(String pullRequestLink) {
        Matcher matcher = PULL_REQUEST_REGEX.matcher(pullRequestLink);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid pull request link: " + pullRequestLink);
        }

        this.owner = matcher.group(1);
        this.repo = matcher.group(2);
        this.number = Integer.parseInt(matcher.group(3));
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

    public String toApiUrl() {
        return String.format("https://api.github.com/repos/%s/%s/pulls/%d", this.owner, this.repo, this.number);
    }
}
