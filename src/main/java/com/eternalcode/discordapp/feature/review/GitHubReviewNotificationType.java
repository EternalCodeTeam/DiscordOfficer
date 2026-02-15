package com.eternalcode.discordapp.feature.review;

public enum GitHubReviewNotificationType {

    DM,
    SERVER,
    BOTH;

    public boolean isDmNotify() {
        return switch (this) {
            case DM, BOTH -> true;
            case SERVER -> false;
        };
    }

    public boolean isServerNotify() {
        return switch (this) {
            case SERVER, BOTH -> true;
            case DM -> false;
        };
    }
}
