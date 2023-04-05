package com.eternalcode.discordapp.review;

public class Review {

    private long forumPostId;
    private String pullRequestUrl;
    private String pullRequestAuthor;
    private String PullRequestTitle;

    public Review(long forumPostId, String pullRequestUrl, String pullRequestAuthor, String pullRequestTitle) {
        this.forumPostId = forumPostId;
        this.pullRequestUrl = pullRequestUrl;
        this.pullRequestAuthor = pullRequestAuthor;
        this.PullRequestTitle = pullRequestTitle;
    }

    public long getForumPostId() {
        return this.forumPostId;
    }

    public void setForumPostId(long forumPostId) {
        this.forumPostId = forumPostId;
    }

    public String getPullRequestUrl() {
        return this.pullRequestUrl;
    }

    public void setPullRequestUrl(String pullRequestUrl) {
        this.pullRequestUrl = pullRequestUrl;
    }

    public String getPullRequestAuthor() {
        return this.pullRequestAuthor;
    }

    public void setPullRequestAuthor(String pullRequestAuthor) {
        this.pullRequestAuthor = pullRequestAuthor;
    }

    public String getPullRequestTitle() {
        return this.PullRequestTitle;
    }

    public void setPullRequestTitle(String pullRequestTitle) {
        this.PullRequestTitle = pullRequestTitle;
    }

}
