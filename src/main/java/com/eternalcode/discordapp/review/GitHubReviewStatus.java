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
        if (status == null) {
            return PENDING;
        }
        
        return switch (status.toLowerCase()) {
            case "pending" -> PENDING;
            case "approved" -> APPROVED;
            case "changes requested" -> CHANGES_REQUESTED;
            case "commented" -> COMMENTED;
            case "merged" -> MERGED;
            case "closed" -> CLOSED;
            default -> {
                // Try to match by enum name
                for (GitHubReviewStatus reviewStatus : values()) {
                    if (reviewStatus.name().equalsIgnoreCase(status)) {
                        yield reviewStatus;
                    }
                }
                yield PENDING;
            }
        };
    }

    public String getDisplayName() {
        return this.displayName;
    }
    
    @Override
    public String toString() {
        return this.displayName;
    }
} 
