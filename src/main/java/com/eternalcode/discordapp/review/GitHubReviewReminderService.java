package com.eternalcode.discordapp.review;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.review.database.GitHubReviewMentionRepository;
import com.eternalcode.discordapp.scheduler.Scheduler;
import io.sentry.Sentry;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;

public class GitHubReviewReminderService {

    private static final Logger LOGGER = Logger.getLogger(GitHubReviewReminderService.class.getName());
    private static final Duration GITHUB_API_RATE_LIMIT = Duration.ofSeconds(1);
    private final JDA jda;
    private final GitHubReviewMentionRepository mentionRepository;
    private final Scheduler scheduler;
    private final Duration reminderInterval;
    private final AppConfig appConfig;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private volatile Instant lastGitHubApiCall = Instant.MIN;

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
            LOGGER.warning("GitHub review reminder service is already running");
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
            LOGGER.log(Level.SEVERE, "Unexpected error in reminder scheduling", exception);
        }
    }

    private CompletableFuture<Void> sendReminders() {
        return this.mentionRepository.getReviewersNeedingReminders(this.reminderInterval)
            .thenCompose(this::processReminders)
            .exceptionally(throwable -> {
                Sentry.captureException(throwable);
                LOGGER.log(Level.SEVERE, "Error sending reminders", throwable);
                return null;
            });
    }

    private CompletableFuture<Void> processReminders(List<GitHubReviewMentionRepository.ReviewerReminder> reminders) {
        if (reminders == null || reminders.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Processing " + reminders.size() + " reminders");

        List<CompletableFuture<Void>> reminderFutures = reminders.stream()
            .map(this::sendReminderAsync)
            .toList();

        return CompletableFuture.allOf(reminderFutures.toArray(new CompletableFuture[0]))
            .thenRun(() -> LOGGER.info("Completed processing all reminders"))
            .exceptionally(throwable -> {
                Sentry.captureException(throwable);
                LOGGER.log(Level.SEVERE, "Error processing reminders", throwable);
                return null;
            });
    }

    private CompletableFuture<Void> sendReminderAsync(GitHubReviewMentionRepository.ReviewerReminder reminder) {
        return CompletableFuture.runAsync(() -> this.scheduler.schedule(
            () -> {
                try {
                    this.sendReminder(reminder);
                }
                catch (Exception exception) {
                    Sentry.captureException(exception);
                    LOGGER.log(Level.SEVERE, "Error sending individual reminder", exception);
                }
            }, this.calculateDelayForRateLimit()));
    }

    private Duration calculateDelayForRateLimit() {
        Instant now = Instant.now();
        Instant nextAllowed = this.lastGitHubApiCall.plus(GITHUB_API_RATE_LIMIT);

        if (now.isBefore(nextAllowed)) {
            return Duration.between(now, nextAllowed);
        }

        return Duration.ZERO;
    }

    private void sendReminder(GitHubReviewMentionRepository.ReviewerReminder reminder) {
        if (!isRunning.get()) {
            return;
        }

        String reviewUrl = reminder.pullRequestUrl();
        GitHubPullRequest pullRequest = GitHubPullRequest.fromUrl(reviewUrl).orNull();
        if (pullRequest == null) {
            LOGGER.warning("Invalid pull request URL: " + reviewUrl);
            return;
        }

        lastGitHubApiCall = Instant.now();

        try {
            boolean isMerged = GitHubReviewUtil.isPullRequestMerged(pullRequest, appConfig.githubToken);

            scheduler.schedule(
                () -> {
                    try {
                        boolean isClosed = GitHubReviewUtil.isPullRequestClosed(pullRequest, appConfig.githubToken);
                        handlePRStatusCheck(reminder, pullRequest, isMerged, isClosed);
                    }
                    catch (IOException exception) {
                        Sentry.captureException(exception);
                        LOGGER.log(Level.SEVERE, "Error checking PR closed status: " + reviewUrl, exception);
                    }
                }, GITHUB_API_RATE_LIMIT);
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            LOGGER.log(Level.SEVERE, "Error checking PR merged status: " + reviewUrl, exception);
        }
    }

    private void handlePRStatusCheck(
        GitHubReviewMentionRepository.ReviewerReminder reminder,
        GitHubPullRequest pullRequest, boolean isMerged, boolean isClosed) {
        if (isMerged || isClosed) {
            this.handleClosedOrMergedPR(reminder.threadId(), isMerged);
            LOGGER.info(
                "PR is " + (isMerged ? "merged" : "closed") + ", skipping reminder for thread: " + reminder.threadId());
            return;
        }

        String githubUsername = this.findGithubUsernameByDiscordId(reminder.userId());
        if (githubUsername == null) {
            LOGGER.warning("Could not find GitHub username for Discord userId " + reminder.userId());
            return;
        }

        this.scheduler.schedule(
            () -> {
                try {
                    boolean alreadyReviewed =
                        GitHubReviewUtil.hasUserReviewed(pullRequest, this.appConfig.githubToken, githubUsername);
                    if (alreadyReviewed) {
                        LOGGER.info("User " + githubUsername + " already reviewed PR, skipping reminder.");
                        return;
                    }

                    this.jda.retrieveUserById(reminder.userId()).queue(
                        user -> this.handleUserRetrieved(
                            user,
                            reminder.threadId(),
                            reminder.pullRequestUrl(),
                            pullRequest),
                        throwable -> {
                            Sentry.captureException(throwable);
                            LOGGER.log(Level.SEVERE, "Error retrieving user: " + reminder.userId(), throwable);
                        }
                    );
                }
                catch (Exception exception) {
                    Sentry.captureException(exception);
                    LOGGER.log(Level.WARNING, "Error checking if user reviewed PR: " + reminder.pullRequestUrl(), exception);
                }
            }, GITHUB_API_RATE_LIMIT);
    }

    private void handleClosedOrMergedPR(long threadId, boolean isMerged) {
        ThreadChannel thread = this.jda.getThreadChannelById(threadId);
        if (thread != null) {
            AppConfig.ReviewSystem reviewSystem = this.appConfig.reviewSystem;
            long tagId = isMerged ? reviewSystem.mergedTagId : reviewSystem.closedTagId;

            thread.getManager()
                .setAppliedTags(ForumTagSnowflake.fromId(tagId))
                .setLocked(true)
                .setArchived(true)
                .queue(
                    success -> LOGGER.info(
                        "Successfully archived " + (isMerged ? "merged" : "closed") + " thread: " + threadId),
                    failure -> LOGGER.log(Level.WARNING, "Failed to archive thread: " + threadId, failure)
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

    private void handleUserRetrieved(User user, long threadId, String pullRequestUrl, GitHubPullRequest pullRequest) {
        if (user == null) {
            LOGGER.warning("User is null for thread: " + threadId);
            return;
        }

        ThreadChannel thread = this.jda.getThreadChannelById(threadId);
        if (thread == null) {
            LOGGER.warning("Could not find thread with ID " + threadId);
            return;
        }

        this.sendReminderMessage(user, thread, pullRequestUrl, pullRequest);
    }

    private void sendReminderMessage(
        User user,
        ThreadChannel thread,
        String pullRequestUrl,
        GitHubPullRequest pullRequest) {
        String message = String.format(
            "Hey %s! you have been assigned as a reviewer for this pull request: <%s>.",
            user.getAsMention(),
            pullRequestUrl
        );

        thread.sendMessage(message).queue(
            success -> {
                LOGGER.info("Reminder sent to " + user.getName() + " for PR: " + pullRequestUrl);
                this.mentionRepository.recordReminderSent(pullRequest, user.getIdLong())
                    .exceptionally(FutureHandler::handleException);
            },
            throwable -> {
                Sentry.captureException(throwable);
                LOGGER.log(Level.SEVERE, "Error sending reminder message to " + user.getName(), throwable);
            }
        );
    }
}
