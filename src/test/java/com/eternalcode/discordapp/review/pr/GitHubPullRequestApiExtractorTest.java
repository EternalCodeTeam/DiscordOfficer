package com.eternalcode.discordapp.review.pr;

import com.eternalcode.discordapp.review.GitHubPullRequestApiExtractor;
import com.eternalcode.discordapp.review.GitHubPullRequestInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubPullRequestApiExtractorTest {

    @Test
    void testExtractPRInfoFromLink() {
        String pullRequestLink = "https://github.com/EternalCodeTeam/DiscordOfficer/pull/123";
        GitHubPullRequestInfo expectedPRInfo = new GitHubPullRequestInfo("EternalCodeTeam", "DiscordOfficer", 123);

        GitHubPullRequestInfo actualPRInfo = GitHubPullRequestApiExtractor.extract(pullRequestLink);

        assertEquals(expectedPRInfo.getOwner(), actualPRInfo.getOwner());
        assertEquals(expectedPRInfo.getRepo(), actualPRInfo.getRepo());
        assertEquals(expectedPRInfo.getNumber(), actualPRInfo.getNumber());
    }


}