package com.eternalcode.discordapp.review;

public enum GitHubReviewStatus {

    PENDING("Pending"),
    APPROVED("Approved"),
    CHANGES_REQUESTED("Changes Requested"),
    COMMENTED("Commented"),
    MERGED("Merged"),
    CLOSED("Closed");

    private final String displayName;

    GitHubReviewStatus(String displayName) {
        this.displayName = displayName;
    }

    public static GitHubReviewStatus fromString(String status) {
        for (GitHubReviewStatus reviewStatus : GitHubReviewStatus.values()) {
            if (reviewStatus.name().equalsIgnoreCase(status) ||
                reviewStatus.getDisplayName().equalsIgnoreCase(status)) {
                return reviewStatus;
            }
        }
        return PENDING;
    }

    public String getDisplayName() {
        return this.displayName;
    }
} 
