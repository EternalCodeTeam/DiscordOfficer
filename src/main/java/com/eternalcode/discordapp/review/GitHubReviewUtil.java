package com.eternalcode.discordapp.review;

public final class GitHubReviewUtil {

    private static final String GITHUB_PULL_REQUEST_REGEX = "^https:\\/\\/github\\.com\\/([a-zA-Z0-9-_]+\\/[a-zA-Z0-9-_]+)\\/pull\\/([0-9]+)$";
    private static final String GITHUB_PULL_REQUEST_TITLE_CONVENTION = "^(GH)-\\d+ .+$";

    public static boolean isPullRequestUrl(String url) {
        return url.matches(GITHUB_PULL_REQUEST_REGEX);
    }

    public static boolean isPullRequestTitleValid(String title) {
        return title.matches(GITHUB_PULL_REQUEST_TITLE_CONVENTION);
    }

}
