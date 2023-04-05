package com.eternalcode.discordapp.review;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "officer_reviews")
class ReviewWrapper {

    @DatabaseField(id = true)
    private long forumPostId;

    @DatabaseField()
    private String pullRequestUrl;

    @DatabaseField()
    private String pullRequestAuthor;

    @DatabaseField()
    private String pullRequestTitle;

    public ReviewWrapper() {
    }

    public ReviewWrapper(long forumPostId, String pullRequestUrl, String pullRequestAuthor, String pullRequestTitle) {
        this.forumPostId = forumPostId;
        this.pullRequestUrl = pullRequestUrl;
        this.pullRequestAuthor = pullRequestAuthor;
        this.pullRequestTitle = pullRequestTitle;
    }

    public static ReviewWrapper from(Review review) {
        return new ReviewWrapper(review.getForumPostId(), review.getPullRequestUrl(), review.getPullRequestAuthor(), review.getPullRequestTitle());
    }

    public Review toReview() {
        return new Review(this.forumPostId, this.pullRequestUrl, this.pullRequestAuthor, this.pullRequestTitle);
    }

}
