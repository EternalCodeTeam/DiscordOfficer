package com.eternalcode.discordapp.review;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubReviewUtilTest {

    @Test
    void isPullRequestUrl() {
        assertTrue(GitHubReviewUtil.isPullRequestUrl("https://github.com/eternalcodeteam/discordofficer/pull/1"));
        assertFalse(GitHubReviewUtil.isPullRequestUrl("https://github.com/eternalcodeteam/discordofficer/issue/1"));
    }

    @Test
    void isPullRequestTitleValid() {
        assertTrue(GitHubReviewUtil.isPullRequestTitleValid("GH-123 Add new feature"));
        assertFalse(GitHubReviewUtil.isPullRequestTitleValid("GH-abc Add new feature"));
        assertFalse(GitHubReviewUtil.isPullRequestTitleValid("Add new feature"));
    }

}