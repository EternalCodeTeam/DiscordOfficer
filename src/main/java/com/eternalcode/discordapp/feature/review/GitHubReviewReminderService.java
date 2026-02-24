package com.eternalcode.discordapp.feature.review;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.feature.review.GitHubReviewUtil.PullRequestState;
import com.eternalcode.discordapp.feature.review.database.GitHubReviewMentionRepository;
import com.eternalcode.discordapp.feature.review.database.GitHubReviewMentionRepository.ReviewerReminder;
import io.sentry.Sentry;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.apache.commons.lang3.ThreadUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubReviewReminderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubReviewReminderService.class);
    private static final Duration GITHUB_API_RATE_LIMIT = Duration.ofSeconds(1);
    private final JDA jda;
    private final GitHubReviewMentionRepository mentionRepository;
    private final Duration reminderInterval;
    private final AppConfig appConfig;
    private final ScheduledExecutorService delayScheduler;

    public GitHubReviewReminderService(
        JDA jda,
        GitHubReviewMentionRepository mentionRepository,
        AppConfig appConfig,
        Duration reminderInterval
    ) {
        this.jda = jda;
        this.mentionRepository = mentionRepository;
        this.reminderInterval = reminderInterval;
        this.appConfig = appConfig;
        this.delayScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        LOGGER.info("Starting GitHub review reminder service with interval: " + this.reminderInterval);
        this.delayScheduler.scheduleAtFixedRate(() -> sendRemindersWithErrorHandling(), 1, 1, TimeUnit.MINUTES);
    }

    private void sendRemindersWithErrorHandling() {
        try {
            processReminders();
        } catch (Exception exception) {
            Sentry.captureException(exception);
            LOGGER.error("Unexpected error in reminder scheduling", exception);
        }
    }

    private void processReminders() {
        List<ReviewerReminder> reminders = this.mentionRepository.getReviewersNeedingReminders(this.reminderInterval);

        LOGGER.info("Processing " + reminders.size() + " reminders");
        for (ReviewerReminder reminder : reminders) {
            this.sendReminder(reminder);
            this.delay();
        }

        LOGGER.info("Completed processing all reminders");
    }

    private void delay() {
        try {
            ThreadUtils.sleep(GitHubReviewReminderService.GITHUB_API_RATE_LIMIT);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception);
        }
    }

    private void sendReminder(ReviewerReminder reminder) {
        String reviewUrl = reminder.pullRequestUrl();
        try {
            GitHubPullRequest pullRequest = GitHubPullRequest.fromUrl(reviewUrl).orNull();
            if (pullRequest == null) {
                LOGGER.warn("Invalid pull request URL: {}", reviewUrl);
                return;
            }

            PullRequestState pullRequestState = GitHubReviewUtil.getPullRequestState(pullRequest, this.appConfig.githubToken);

            this.handlePRStatusCheck(reminder, pullRequest, pullRequestState);
        } catch (Exception exception) {
            Sentry.captureException(exception);
            LOGGER.error("Error checking PR merged status: {}", reviewUrl, exception);
        }
    }

    private void handlePRStatusCheck(ReviewerReminder reminder, GitHubPullRequest pullRequest, PullRequestState state) {
        if (state.merged() || state.closed()) {
            this.handleClosedOrMergedPR(reminder.threadId(), pullRequest, state.merged());
            LOGGER.info(
                "PR is {}, skipping reminder for thread: {}",
                state.merged() ? "merged" : "closed",
                reminder.threadId());
            return;
        }

        String githubUsername = this.findGithubUsernameByDiscordId(reminder.userId());
        if (githubUsername == null) {
            LOGGER.warn("Could not find GitHub username for Discord userId {}", reminder.userId());
            return;
        }

        List<String> requestedReviewers = GitHubReviewUtil.getReviewers(pullRequest, this.appConfig.githubToken);
        if (!this.isStillRequestedReviewer(requestedReviewers, githubUsername)) {
            LOGGER.info(
                "Skipping reminder for {} on PR {} because user is no longer in requested reviewers",
                githubUsername,
                pullRequest.toUrl()
            );
            return;
        }

        boolean alreadyReviewed =
            GitHubReviewUtil.hasUserReviewed(pullRequest, this.appConfig.githubToken, githubUsername);
        if (alreadyReviewed) {
            LOGGER.info("User {} already reviewed PR, skipping reminder.", githubUsername);
            return;
        }


        this.handleUserRetrieved(
            reminder,
            pullRequest
        );
    }

    private boolean isStillRequestedReviewer(List<String> requestedReviewers, String githubUsername) {
        if (githubUsername == null || githubUsername.trim().isEmpty()) {
            return false;
        }

        if (requestedReviewers == null || requestedReviewers.isEmpty()) {
            return false;
        }

        return requestedReviewers.stream()
            .anyMatch(requestedReviewer -> requestedReviewer != null && requestedReviewer.equalsIgnoreCase(githubUsername));
    }

    private void handleClosedOrMergedPR(long threadId, GitHubPullRequest pullRequest, boolean isMerged) {
        GitHubReviewStatus status = isMerged ? GitHubReviewStatus.MERGED : GitHubReviewStatus.CLOSED;
        this.mentionRepository.updateReviewStatus(pullRequest, status);

        ThreadChannel thread = this.jda.getThreadChannelById(threadId);
        if (thread != null) {
            thread.delete().complete();
            LOGGER.info("Successfully deleted {} thread: {}",
                isMerged ? "merged" : "closed",
                threadId
            );
        }
    }

    @Nullable
    private String findGithubUsernameByDiscordId(long userId) {
        return this.appConfig.reviewSystem.reviewers.stream()
            .filter(user -> user.getDiscordId() != null && user.getDiscordId() == userId)
            .map(gitHubReviewUser -> gitHubReviewUser.getGithubUsername())
            .findFirst()
            .orElse(null);
    }

    private void handleUserRetrieved(ReviewerReminder reminder, GitHubPullRequest pullRequest) {
        User user = this.jda.retrieveUserById(reminder.userId()).complete();
        if (user == null) {
            LOGGER.warn("User is null for thread: {}", reminder.threadId());
            return;
        }

        ThreadChannel thread = this.jda.getThreadChannelById(reminder.threadId());
        if (thread == null) {
            LOGGER.warn("Could not find thread with ID {}", reminder.threadId());
            return;
        }

        this.sendReminderMessage(user, thread, reminder.pullRequestUrl(), pullRequest);
    }

    private void sendReminderMessage(
        User user,
        ThreadChannel thread,
        String pullRequestUrl,
        GitHubPullRequest pullRequest
    ) {
        String message = String.format(
            "Hey %s! you have been assigned as a reviewer for this pull request: <%s>.",
            user.getAsMention(),
            pullRequestUrl
        );

        thread.sendMessage(message).complete();
        LOGGER.info("Reminder sent to {} for PR: {}", user.getName(), pullRequestUrl);
        this.mentionRepository.recordReminderSent(pullRequest, user.getIdLong());
    }

    public void stop() {
        this.delayScheduler.shutdownNow();
        try {
            if (!this.delayScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.warn("Reminder scheduler did not terminate in time, forcing shutdown.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while waiting for reminder scheduler to terminate", exception);
        }
    }

}

