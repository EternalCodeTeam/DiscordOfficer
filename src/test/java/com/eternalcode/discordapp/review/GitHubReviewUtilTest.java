package com.eternalcode.discordapp.review;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GitHubReviewUtilTest {

    private static final String GITHUB_FAKE_TOKEN = "fake-token";

    private MockWebServer server;

    @BeforeEach
    public void setUp() throws IOException {
        this.server = new MockWebServer();
        this.server.start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.server.shutdown();
    }

    @Test
    void testIsPullRequestUrl() {
        assertTrue(GitHubReviewUtil.isPullRequestUrl("https://github.com/user/repo/pull/123"));
        assertTrue(GitHubReviewUtil.isPullRequestUrl("https://github.com/DiscordOfficer/DiscordOfficer/pull/456"));
        assertFalse(GitHubReviewUtil.isPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/pull"));
        assertFalse(GitHubReviewUtil.isPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/123"));
        assertFalse(GitHubReviewUtil.isPullRequestUrl("https://github.com/EternalCodeTeam/DiscordOfficer/pull/123/"));
    }

    @Test
    public void testIsPullRequestTitleValid() {
        assertTrue(GitHubReviewUtil.isPullRequestTitleValid("GH-123 This is a valid title"));
        assertFalse(GitHubReviewUtil.isPullRequestTitleValid("This is an invalid title"));
        assertFalse(GitHubReviewUtil.isPullRequestTitleValid("GH- This title has an invalid number"));
    }

    @Test
    public void testGetReviewers() throws Exception {
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

        HttpUrl baseUrl = this.server.url("/");

        List<String> reviewers = GitHubReviewUtil.getReviewers(baseUrl.toString(), GITHUB_FAKE_TOKEN);

        assertNotNull(reviewers);
        assertEquals(2, reviewers.size());
        assertEquals("Martin", reviewers.get(0));
        assertEquals("Piotr", reviewers.get(1));

        RecordedRequest recordedRequest = this.server.takeRequest();
        assertEquals(baseUrl.toString(), recordedRequest.getRequestUrl().toString());
        assertEquals("token" + GITHUB_FAKE_TOKEN, recordedRequest.getHeader("Authorization"));
    }

    @Test
    public void testGetGitHubPullRequestApiUrl() {
        String url = "https://github.com/EternalCodeTeam/DiscordOfficer/pull/123";
        String expectedApiUrl = "https://api.github.com/repos/EternalCodeTeam/DiscordOfficer/pulls/123";

        String actualApiUrl = GitHubReviewUtil.getGitHubPullRequestApiUrl(url);

        assertEquals(expectedApiUrl, actualApiUrl);
    }

}
