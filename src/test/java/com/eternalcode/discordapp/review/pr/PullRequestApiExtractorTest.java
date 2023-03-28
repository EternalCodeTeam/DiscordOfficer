package com.eternalcode.discordapp.review.pr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PullRequestApiExtractorTest {

    @Test
    void testExtractPRInfoFromLink() {
        String pullRequestLink = "https://github.com/EternalCodeTeam/DiscordOfficer/pull/123";
        PullRequestInfo expectedPRInfo = new PullRequestInfo("EternalCodeTeam", "DiscordOfficer", 123);

        PullRequestInfo actualPRInfo = PullRequestApiExtractor.extractPRInfoFromLink(pullRequestLink);

        assertEquals(expectedPRInfo.getOwner(), actualPRInfo.getOwner());
        assertEquals(expectedPRInfo.getRepo(), actualPRInfo.getRepo());
        assertEquals(expectedPRInfo.getNumber(), actualPRInfo.getNumber());
    }


}