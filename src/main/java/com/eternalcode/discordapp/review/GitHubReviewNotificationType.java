package com.eternalcode.discordapp.review;

public enum GitHubReviewNotificationType {

    DM,
    SERVER,
    BOTH;

    public boolean isDmNotify() {
        return this == DM || this == BOTH;
    }

    public boolean isServerNotify() {
        return this == SERVER || this == BOTH;
    }
}
