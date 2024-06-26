package com.eternalcode.discordapp.review;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import panda.std.Result;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
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
    @DisplayName("Test isPullRequestUrl")
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

    @TestFactory
    @DisplayName("Test isPullRequestTitleValid with valid titles")
    Stream<DynamicTest> testValidPullRequestTitles() {
        return Stream.of(
                "GH-123 This is a valid title",
                "GH-123 Another valid title"
            )
            .map(title -> dynamicTest("Valid Title: " + title, () -> assertTrue(GitHubReviewUtil.isPullRequestTitleValid(title))));
    }

    @TestFactory
    @DisplayName("Test isPullRequestTitleValid with invalid titles")
    Stream<DynamicTest> testInvalidPullRequestTitles() {
        return Stream.of(
                "This is an invalid title",
                "GH- This title has an invalid number"
            )
            .map(title -> dynamicTest("Invalid Title: " + title, () -> assertFalse(GitHubReviewUtil.isPullRequestTitleValid(title))));
    }

    @Test
    @DisplayName("Test isTitleLengthValid with a valid title")
    void testValidTitleLength() {
        String title = "GH-123 This is a valid title";
        assertTrue(GitHubReviewUtil.isTitleLengthValid(title), "Valid Title: " + title);
    }

    @Test
    @DisplayName("Test isTitleLengthValid with an invalid title")
    void testInvalidTitleLength() {
        String title = "GH-123 This is a very long title. It is so long that it should be " +
            "considered invalid, but we are testing it all the same to ensure that our title length validation works as expected.";
        assertFalse(GitHubReviewUtil.isTitleLengthValid(title), "Invalid Title: " + title);
    }

    @Test
    @DisplayName("Test getReviewers functionality")
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

        this.setMockResponse(jsonResponse);

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
    @DisplayName("Test getPullRequestTitleFromUrl function")
    void testGetPullRequestTitleFromUrl() throws IOException {
        String jsonResponse = """
            {
              "title": "Test Pull Request Title"
            }""";

        this.setMockResponse(jsonResponse);

        String title = GitHubReviewUtil.getPullRequestTitleFromUrl(this.fakePullRequest, GITHUB_FAKE_TOKEN);
        assertEquals("Test Pull Request Title", title);
    }

    @Test
    @DisplayName("Test isPullRequestMerged function")
    void testIsPullRequestMerged() throws IOException {
        String jsonResponse = """
            {
              "merged": true
            }""";

        this.setMockResponse(jsonResponse);

        boolean merged = GitHubReviewUtil.isPullRequestMerged(this.fakePullRequest, GITHUB_FAKE_TOKEN);
        assertTrue(merged);
    }

    @Test
    @DisplayName("Test Getting GitHub Pull Request API URL")
    void testGetGitHubPullRequestApiUrl() {
        String url = "https://github.com/EternalCodeTeam/DiscordOfficer/pull/123";
        String expectedApiUrl = "https://api.github.com/repos/EternalCodeTeam/DiscordOfficer/pulls/123";

        Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(url);
        assertTrue(result.isOk());

        GitHubPullRequest pullRequest = result.get();
        assertEquals(expectedApiUrl, pullRequest.toApiUrl());
    }

    @Test
    @DisplayName("Test isPullRequestClosed when state is closed")
    void testIsPullRequestClosedWhenStateIsClosed() throws IOException {
        this.setMockResponse("{\"state\": \"closed\"}");
        boolean isClosed = GitHubReviewUtil.isPullRequestClosed(this.fakePullRequest, GITHUB_FAKE_TOKEN);
        assertTrue(isClosed);
    }

    @Test
    @DisplayName("Test isPullRequestClosed when state is open")
    void testIsPullRequestClosedWhenStateIsOpen() throws IOException {
        this.setMockResponse("{\"state\": \"open\"}");
        boolean isClosed = GitHubReviewUtil.isPullRequestClosed(this.fakePullRequest, GITHUB_FAKE_TOKEN);
        assertFalse(isClosed);
    }

    private void setMockResponse(String jsonResponse) {
        this.server.enqueue(new MockResponse()
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));
    }
}
