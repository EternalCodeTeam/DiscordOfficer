package com.eternalcode.discordapp.review;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import panda.std.Result;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitHubReviewUtilTest {

    private static final String GITHUB_FAKE_TOKEN = "fake-token";

    private MockWebServer server;
    private GitHubPullRequest fakePullRequest;

    @BeforeEach
    public void setUp() throws IOException {
        this.server = new MockWebServer();
        this.server.start();
        this.fakePullRequest = mock(GitHubPullRequest.class);

        when(this.fakePullRequest.toApiUrl()).thenReturn(this.server.url("/").toString());
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.server.shutdown();
    }

    @Test
    void testIsPullRequestUrl() {
        assertPullRequestUrl("https://github.com/user/repo/pull/123", "user", "repo", 123);
        assertPullRequestUrl("https://github.com/DiscordOfficer/DiscordOfficer/pull/456", "DiscordOfficer", "DiscordOfficer", 456);
        assertNotPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/pull");
        assertNotPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/123");
        assertNotPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/pull/123/");
    }

    private void assertPullRequestUrl(String url, String expectedOwner, String expectedRepo, int expectedNumber) {
        Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(url);
        assertTrue(result.isOk());

        GitHubPullRequest pullRequest = result.get();
        assertEquals(expectedOwner, pullRequest.getOwner());
        assertEquals(expectedRepo, pullRequest.getRepository());
        assertEquals(expectedNumber, pullRequest.getNumber());

        assertEquals(url, pullRequest.toUrl());
    }

    private void assertNotPullRequestUrl(String url) {
        Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(url);
        assertTrue(result.isErr());
    }

    @Test
    void testIsPullRequestTitleValid() {
        assertTrue(GitHubReviewUtil.isPullRequestTitleValid("GH-123 This is a valid title"));
        assertFalse(GitHubReviewUtil.isPullRequestTitleValid("This is an invalid title"));
        assertFalse(GitHubReviewUtil.isPullRequestTitleValid("GH- This title has an invalid number"));
    }

    @Test
    void testGetReviewers() throws Exception {
        String jsonResponse = """
                {
                  "requested_reviewers": [
                    {
                      "login": "Martin"
                    },
                    {
                      "login": "Piotr"
                    }
                  ]
                }""";

        this.server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<String> reviewers = GitHubReviewUtil.getReviewers(fakePullRequest, GITHUB_FAKE_TOKEN);

        assertNotNull(reviewers);
        assertEquals(2, reviewers.size());
        assertEquals("Martin", reviewers.get(0));
        assertEquals("Piotr", reviewers.get(1));

        RecordedRequest recordedRequest = this.server.takeRequest();
        assertEquals(fakePullRequest.toApiUrl(), recordedRequest.getRequestUrl().toString());
        assertEquals("token" + GITHUB_FAKE_TOKEN, recordedRequest.getHeader("Authorization"));
    }

    @Test
    void testGetGitHubPullRequestApiUrl() {
        String url = "https://github.com/EternalCodeTeam/DiscordOfficer/pull/123";
        String expectedApiUrl = "https://api.github.com/repos/EternalCodeTeam/DiscordOfficer/pulls/123";

        Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(url);
        assertTrue(result.isOk());

        GitHubPullRequest pullRequest = result.get();
        assertEquals(expectedApiUrl, pullRequest.toApiUrl());
    }

}
