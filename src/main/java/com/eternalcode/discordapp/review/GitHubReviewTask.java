package com.eternalcode.discordapp.review;

import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;

public class GitHubReviewTask implements Runnable {

    private final GitHubReviewService gitHubReviewService;
    private final JDA jda;

    public GitHubReviewTask(GitHubReviewService gitHubReviewService, JDA jda) {
        this.gitHubReviewService = gitHubReviewService;
        this.jda = jda;
    }

    @Override
    public void run() {
        try {
            this.gitHubReviewService.archiveMergedPullRequest(this.jda);
            this.gitHubReviewService.mentionReviewersOnAllReviewChannels(this.jda);
        }
        catch (Exception exception) {
            Sentry.captureException(exception);
            exception.printStackTrace();
        }
    }
}
