package com.eternalcode.discordapp.review.pr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PullRequestApiExtractor {

    private static final Pattern PULL_REQUEST_REGEX = Pattern.compile("https://github\\.com/(\\w+)/([\\w-]+)/pull/(\\d+)");

    public static PullRequestInfo extractPRInfoFromLink(String pullRequestLink) {
        Matcher matcher = PULL_REQUEST_REGEX.matcher(pullRequestLink);

        if (matcher.find()) {
            return new PullRequestInfo(matcher.group(1), matcher.group(2), Integer.parseInt(matcher.group(3)));
        }

        return null;
    }
}
