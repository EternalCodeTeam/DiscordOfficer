package com.eternalcode.discordapp.review;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import panda.std.Result;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        this.assertPullRequestUrl("https://github.com/user/repo/pull/123", "user", "repo", 123);
        this.assertPullRequestUrl("https://github.com/DiscordOfficer/DiscordOfficer/pull/456", "DiscordOfficer", "DiscordOfficer", 456);
        this.assertNotPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/pull");
        this.assertNotPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/123");
        this.assertNotPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/pull/123/");
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
        assertTrue(GitHubReviewUtil.isPullRequestTitleValid("GH-123 Another valid title"));
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

        List<String> reviewers = GitHubReviewUtil.getReviewers(this.fakePullRequest, GITHUB_FAKE_TOKEN);

        assertNotNull(reviewers);
        assertEquals(2, reviewers.size());
        assertEquals("Martin", reviewers.get(0));
        assertEquals("Piotr", reviewers.get(1));

        RecordedRequest recordedRequest = this.server.takeRequest();
        assertEquals(this.fakePullRequest.toApiUrl(), recordedRequest.getRequestUrl().toString());
        assertEquals("token " + GITHUB_FAKE_TOKEN, recordedRequest.getHeader("Authorization"));
    }


    @Test
    void testGetPullRequestTitleFromUrl() throws IOException {
        String jsonResponse = """
            {
              "title": "Test Pull Request Title"
            }""";

        this.server.enqueue(new MockResponse()
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));

        String title = GitHubReviewUtil.getPullRequestTitleFromUrl(this.fakePullRequest, GITHUB_FAKE_TOKEN);
        assertEquals("Test Pull Request Title", title);
    }

    @Test
    void testGetPullRequestTitleFromUrlException() throws IOException {
        this.server.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(IOException.class, () -> GitHubReviewUtil.getPullRequestTitleFromUrl(this.fakePullRequest, GITHUB_FAKE_TOKEN));
    }

    @Test
    void testIsPullRequestMerged() throws IOException {
        String jsonResponse = """
            {
              "merged": true
            }""";

        this.server.enqueue(new MockResponse()
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));

        boolean merged = GitHubReviewUtil.isPullRequestMerged(this.fakePullRequest, GITHUB_FAKE_TOKEN);
        assertTrue(merged);
    }

    @Test
    void testIsPullRequestMergedException() throws IOException {
        this.server.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(IOException.class, () -> GitHubReviewUtil.isPullRequestMerged(this.fakePullRequest, GITHUB_FAKE_TOKEN));
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
