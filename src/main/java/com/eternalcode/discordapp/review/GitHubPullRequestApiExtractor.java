package com.eternalcode.discordapp.review;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitHubPullRequestApiExtractor {

    private static final Pattern PULL_REQUEST_REGEX = Pattern.compile("https://github\\.com/(\\w+)/([\\w-]+)/pull/(\\d+)");

    public static GitHubPullRequestInfo extract(String pullRequestLink) {
        Matcher matcher = PULL_REQUEST_REGEX.matcher(pullRequestLink);

        if (matcher.find()) {
            return new GitHubPullRequestInfo(matcher.group(1), matcher.group(2), Integer.parseInt(matcher.group(3)));
        }

        return null;
    }
}
