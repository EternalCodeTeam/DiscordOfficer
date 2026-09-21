package com.eternalcode.discordapp.feature.releasewatch;

import java.time.Instant;

public record GitHubRelease(
    String tagName,
    String name,
    String htmlUrl,
    String body,
    Instant publishedAt
) {
}
