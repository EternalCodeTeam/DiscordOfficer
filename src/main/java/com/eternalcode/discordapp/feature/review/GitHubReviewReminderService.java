package com.eternalcode.discordapp.feature.review;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.feature.review.database.GitHubReviewMentionRepository;
import com.eternalcode.discordapp.scheduler.Scheduler;
import io.sentry.Sentry;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubReviewReminderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubReviewReminderService.class);
    private static final Duration GITHUB_API_RATE_LIMIT = Duration.ofSeconds(1);
    private final JDA jda;
    private final GitHubReviewMentionRepository mentionRepository;
    private final Scheduler scheduler;
    private final Duration reminderInterval;
    private final AppConfig appConfig;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public GitHubReviewReminderService(
        JDA jda,
        GitHubReviewMentionRepository mentionRepository,
        AppConfig appConfig,
        Scheduler scheduler,
        Duration reminderInterval
    ) {
        this.jda = jda;
        this.mentionRepository = mentionRepository;
        this.reminderInterval = reminderInterval;
        this.scheduler = scheduler;
        this.appConfig = appConfig;
    }

    public void start() {
        if (this.isRunning.compareAndSet(false, true)) {
            LOGGER.info("Starting GitHub review reminder service with interval: " + this.reminderInterval);

            this.scheduler.scheduleRepeating(
                this::sendRemindersWithErrorHandling,
                Duration.ofMinutes(1),
                this.reminderInterval
            );

            LOGGER.info("GitHub review reminder service started");
        }
        else {
            LOGGER.warn("GitHub review reminder service is already running");
        }
    }

    public void stop() {
        if (this.isRunning.compareAndSet(true, false)) {
            LOGGER.info("GitHub review reminder service stopped");
        }
    }

    private void sendRemindersWithErrorHandling() {
        if (!this.isRunning.get()) {
            return;
        }

        try {
            this.sendReminders().exceptionally(FutureHandler::handleException);
        }
        catch (Exception exception) {
            Sentry.captureException(exception);
            LOGGER.error("Unexpected error in reminder scheduling", exception);
        }
    }

    private CompletableFuture<Void> sendReminders() {
        return this.mentionRepository.getReviewersNeedingReminders(this.reminderInterval)
            .thenCompose(this::processReminders)
            .exceptionally(throwable -> {
                Sentry.captureException(throwable);
                LOGGER.error("Error sending reminders", throwable);
                return null;
            });
    }

    private CompletableFuture<Void> processReminders(List<GitHubReviewMentionRepository.ReviewerReminder> reminders) {
        if (reminders == null || reminders.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Processing " + reminders.size() + " reminders");

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (GitHubReviewMentionRepository.ReviewerReminder reminder : reminders) {
            chain = chain
                .thenCompose(unused -> this.sendReminder(reminder))
                .thenCompose(unused -> this.delay(GITHUB_API_RATE_LIMIT));
        }

        return chain
            .thenRun(() -> LOGGER.info("Completed processing all reminders"))
            .exceptionally(throwable -> {
                Sentry.captureException(throwable);
                LOGGER.error("Error processing reminders", throwable);
                return null;
            });
    }

    private CompletableFuture<Void> delay(Duration delay) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.scheduler.schedule(() -> future.complete(null), delay);
        return future;
    }

    private CompletableFuture<Void> sendReminder(GitHubReviewMentionRepository.ReviewerReminder reminder) {
        if (!this.isRunning.get()) {
            return CompletableFuture.completedFuture(null);
        }

        String reviewUrl = reminder.pullRequestUrl();
        GitHubPullRequest pullRequest = GitHubPullRequest.fromUrl(reviewUrl).orNull();
        if (pullRequest == null) {
            LOGGER.warn("Invalid pull request URL: {}", reviewUrl);
            return CompletableFuture.completedFuture(null);
        }

        try {
            GitHubReviewUtil.PullRequestState pullRequestState =
                GitHubReviewUtil.getPullRequestState(pullRequest, this.appConfig.githubToken);

            return this.handlePRStatusCheck(reminder, pullRequest, pullRequestState.merged(), pullRequestState.closed());
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            LOGGER.error("Error checking PR merged status: {}", reviewUrl, exception);
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> handlePRStatusCheck(
        GitHubReviewMentionRepository.ReviewerReminder reminder,
        GitHubPullRequest pullRequest, boolean isMerged, boolean isClosed) {
        if (isMerged || isClosed) {
            this.handleClosedOrMergedPR(reminder.threadId(), pullRequest, isMerged);
            LOGGER.info(
                "PR is {}, skipping reminder for thread: {}",
                isMerged ? "merged" : "closed",
                reminder.threadId());
            return CompletableFuture.completedFuture(null);
        }

        String githubUsername = this.findGithubUsernameByDiscordId(reminder.userId());
        if (githubUsername == null) {
            LOGGER.warn("Could not find GitHub username for Discord userId {}", reminder.userId());
            return CompletableFuture.completedFuture(null);
        }

        List<String> requestedReviewers = GitHubReviewUtil.getReviewers(pullRequest, this.appConfig.githubToken);
        if (!this.isStillRequestedReviewer(requestedReviewers, githubUsername)) {
            LOGGER.info(
                "Skipping reminder for {} on PR {} because user is no longer in requested reviewers",
                githubUsername,
                pullRequest.toUrl()
            );
            return CompletableFuture.completedFuture(null);
        }

        try {
            boolean alreadyReviewed =
                GitHubReviewUtil.hasUserReviewed(pullRequest, this.appConfig.githubToken, githubUsername);
            if (alreadyReviewed) {
                LOGGER.info("User {} already reviewed PR, skipping reminder.", githubUsername);
                return CompletableFuture.completedFuture(null);
            }

            CompletableFuture<Void> reminderFuture = new CompletableFuture<>();
            this.jda.retrieveUserById(reminder.userId()).queue(
                user -> this.handleUserRetrieved(
                    user,
                    reminder.threadId(),
                    reminder.pullRequestUrl(),
                    pullRequest
                ).whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        reminderFuture.completeExceptionally(throwable);
                        return;
                    }
                    reminderFuture.complete(null);
                }),
                throwable -> {
                    Sentry.captureException(throwable);
                    LOGGER.error("Error retrieving user: {}", reminder.userId(), throwable);
                    reminderFuture.completeExceptionally(throwable);
                }
            );
            return reminderFuture.exceptionally(throwable -> null);
        }
        catch (Exception exception) {
            Sentry.captureException(exception);
            LOGGER.warn("Error checking if user reviewed PR: {}", reminder.pullRequestUrl(), exception);
            return CompletableFuture.completedFuture(null);
        }
    }

    private boolean isStillRequestedReviewer(List<String> requestedReviewers, String githubUsername) {
        if (githubUsername == null || githubUsername.trim().isEmpty()) {
            return false;
        }

        if (requestedReviewers == null || requestedReviewers.isEmpty()) {
            return false;
        }

        return requestedReviewers.stream()
            .anyMatch(requestedReviewer -> requestedReviewer != null &&
                requestedReviewer.equalsIgnoreCase(githubUsername));
    }

    private void handleClosedOrMergedPR(long threadId, GitHubPullRequest pullRequest, boolean isMerged) {
        GitHubReviewStatus status = isMerged ? GitHubReviewStatus.MERGED : GitHubReviewStatus.CLOSED;
        this.mentionRepository.updateReviewStatus(pullRequest, status)
            .exceptionally(FutureHandler::handleException);

        ThreadChannel thread = this.jda.getThreadChannelById(threadId);
        if (thread != null) {
            thread.delete()
                .queue(
                    success -> LOGGER.info(
                        "Successfully deleted {} thread: {}",
                        isMerged ? "merged" : "closed",
                        threadId),
                    failure -> LOGGER.warn("Failed to delete thread: {}", threadId, failure)
                );
        }
    }

    private String findGithubUsernameByDiscordId(long userId) {
        return this.appConfig.reviewSystem.reviewers.stream()
            .filter(user -> user.getDiscordId() != null && user.getDiscordId() == userId)
            .map(GitHubReviewUser::getGithubUsername)
            .findFirst()
            .orElse(null);
    }

    private CompletableFuture<Void> handleUserRetrieved(
        User user, long threadId, String pullRequestUrl, GitHubPullRequest pullRequest
    ) {
        if (user == null) {
            LOGGER.warn("User is null for thread: {}", threadId);
            return CompletableFuture.completedFuture(null);
        }

        ThreadChannel thread = this.jda.getThreadChannelById(threadId);
        if (thread == null) {
            LOGGER.warn("Could not find thread with ID {}", threadId);
            return CompletableFuture.completedFuture(null);
        }

        return this.sendReminderMessage(user, thread, pullRequestUrl, pullRequest);
    }

    private CompletableFuture<Void> sendReminderMessage(
        User user,
        ThreadChannel thread,
        String pullRequestUrl,
        GitHubPullRequest pullRequest) {
        String message = String.format(
            "Hey %s! you have been assigned as a reviewer for this pull request: <%s>.",
            user.getAsMention(),
            pullRequestUrl
        );

        CompletableFuture<Void> messageFuture = new CompletableFuture<>();
        thread.sendMessage(message).queue(
            success -> {
                LOGGER.info("Reminder sent to {} for PR: {}", user.getName(), pullRequestUrl);
                this.mentionRepository.recordReminderSent(pullRequest, user.getIdLong())
                    .thenRun(() -> messageFuture.complete(null))
                    .exceptionally(throwable -> {
                        FutureHandler.handleException(throwable);
                        messageFuture.complete(null);
                        return null;
                    });
            },
            throwable -> {
                Sentry.captureException(throwable);
                LOGGER.error("Error sending reminder message to {}", user.getName(), throwable);
                messageFuture.completeExceptionally(throwable);
            }
        );
        return messageFuture.exceptionally(throwable -> null);
    }
}

