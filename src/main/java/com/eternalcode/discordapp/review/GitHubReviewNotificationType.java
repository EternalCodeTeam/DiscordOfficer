package com.eternalcode.discordapp.review;

public enum GitHubReviewNotificationType {

    DM,
    SERVER,
    NONE,
    BOTH;

    public boolean isDmNotify() {
        return switch (this) {
            case DM, BOTH -> true;
            case SERVER, NONE -> false;
        };
    }

    public boolean isServerNotify() {
        return switch (this) {
            case SERVER, BOTH -> true;
            case DM, NONE -> false;
        };
    }
}
